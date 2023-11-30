package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.prepareForm
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.ContentTypeMatcher
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.discovery.OpenIDConnectDiscover
import org.publicvalue.multiplatform.oidc.flows.PKCE
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.TokenRequest
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import org.publicvalue.multiplatform.oidc.types.remote.OpenIDConnectConfiguration
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalSerializationApi::class, ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIDConnectClient", name = "OpenIDConnectClient", exact = true)
class OpenIDConnectClient(
    val httpClient: HttpClient = DefaultHttpClient,
    val config: OpenIDConnectClientConfig,
) {
    constructor(config: OpenIDConnectClientConfig): this(httpClient = DefaultHttpClient, config = config)

    var discoverDocument: OpenIDConnectConfiguration? = null

    companion object {
        val DefaultHttpClient by lazy {
            HttpClient {
                install(ContentNegotiation) {
                    // register custom type matcher to support broken IDPs that don't correct content-type
                    register(
                        contentTypeToSend = ContentType.Application.Json,
                        converter = KotlinxSerializationConverter(
                            Json {
                                explicitNulls = false
                                ignoreUnknownKeys = true
                            }
                        ),
                        contentTypeMatcher = object : ContentTypeMatcher {
                            override fun contains(contentType: ContentType): Boolean {
                                return true
                            }
                        }
                    ) {
                    }
                }
            }
        }
    }

    init {
        config.validate()
    }

    fun createAuthorizationCodeRequest(): AuthCodeRequest {
        val pkce = PKCE(config.codeChallengeMethod)
        val nonce = randomBytes().encodeForPKCE()
        val state = randomBytes().encodeForPKCE()

        val url = URLBuilder(config.endpoints.authorizationEndpoint!!).apply {
            parameters.append("client_id", config.clientId!!)
            config.clientSecret?.let { parameters.append("client_secret", it) }
            parameters.append("response_type", "code")
            parameters.append("response_mode", "query")
            config.scope?.let { parameters.append("scope", it) }
            parameters.append("nonce", nonce)
            config.codeChallengeMethod.queryString?.let { parameters.append("code_challenge_method", it) }
            if (config.codeChallengeMethod != CodeChallengeMethod.off) { parameters.append("code_challenge", pkce.codeChallenge) }
            config.redirectUri?.let { parameters.append("redirect_uri", it) }
            parameters.append("state", state)
        }.build()

        return AuthCodeRequest(
            url, config, pkce, state, nonce
        )
    }

    suspend fun discover() {
        config.discoveryUri?.let { discoveryUri ->
            val config = OpenIDConnectDiscover(httpClient).downloadConfiguration(discoveryUri)
            this.config.updateWithDiscovery(config)
            discoverDocument = config
        } ?: run {
            throw OpenIDConnectException.InvalidUrl("No discoveryUri set")
        }
    }

    /**
     * https://openid.net/specs/openid-connect-rpinitiated-1_0.html
     */
    suspend fun endSession(tokens: AccessTokenResponse): HttpStatusCode {
        val endpoint = config.endpoints.endSessionEndpoint?.trim()
        if (!endpoint.isNullOrEmpty()) {
            val url = URLBuilder(endpoint)
            val response = httpClient.post {
                this.url(url.build())
                parameter("id_token_hint", tokens.id_token)
            }
            return response.status
        } else {
            throw OpenIDConnectException.InvalidUrl("No endSessionEndpoint set")
        }
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3
     * + code verifier https://datatracker.ietf.org/doc/html/rfc7636#section-4.5
     */
    suspend fun exchangeToken(authCodeRequest: AuthCodeRequest, code: String): AccessTokenResponse {
        // send code_verifier to server
        val tokenRequest = createAccessTokenRequest(authCodeRequest, code)
        return executeTokenRequest(tokenRequest.request)
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-6
     */
    suspend fun refreshToken(tokens: AccessTokenResponse): AccessTokenResponse {
        val tokenRequest = createRefreshTokenRequest(tokens)
        return executeTokenRequest(tokenRequest.request)
    }

    suspend fun createAccessTokenRequest(authCodeRequest: AuthCodeRequest, code: String): TokenRequest {
        val url = URLBuilder(config.endpoints?.tokenEndpoint!!).build()

        val formParameters = parameters {
            append("grant_type", "authorization_code")
            append("code", code)
            config.redirectUri?.let { append("redirect_uri", it) }
            append("client_id", config.clientId!!)
            config.clientSecret?.let { append("client_secret", it) }
            if (config.codeChallengeMethod != CodeChallengeMethod.off) { append("code_verifier", authCodeRequest.pkce.codeVerifier) }
        }
        val request = httpClient.prepareForm(
            formParameters = formParameters
        ) {
            url(url)
        }
        return TokenRequest(
            request,
            formParameters
        )
    }

    suspend fun createRefreshTokenRequest(tokens: AccessTokenResponse): TokenRequest {
        val url = URLBuilder(config.endpoints.tokenEndpoint!!).build()

        val formParameters = parameters {
            append("grant_type", "refresh_token")
            append("client_id", config.clientId!!)
            config.clientSecret?.let { append("client_secret", it) }
            append("refresh_token", tokens.refresh_token ?: "")
            config.scope?.let { append("scope", it) }
        }
        val request = httpClient.prepareForm(
            formParameters = formParameters
        ) {
            url(url)
        }
        return TokenRequest(
            request,
            formParameters
        )
    }

    private suspend fun executeTokenRequest(httpFormRequest: HttpStatement): AccessTokenResponse {
        val response = httpFormRequest.execute()
        return if (response.status.isSuccess()) {
            try {
                val accessTokenResponse: AccessTokenResponse = response.call.body()
                accessTokenResponse
            } catch (e: NoTransformationFoundException) {
                throw response.toOpenIdConnectException()
            } catch (e: JsonConvertException) {
                throw response.toOpenIdConnectException()
            }
        } else {
            throw response.toOpenIdConnectException()
        }
    }

    private suspend fun HttpResponse.toOpenIdConnectException(): OpenIDConnectException.UnsuccessfulTokenRequest {
        val errorResponse = call.errorBody()
        val body = call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
        return OpenIDConnectException.UnsuccessfulTokenRequest(
            message = "Exchange token failed: ${status.value} ${errorResponse?.error_description}",
            statusCode = status,
            body = body,
            errorResponse = errorResponse
        )
    }
}

private suspend fun HttpClientCall.errorBody(): ErrorResponse? {
    return try {
        body<ErrorResponse>()
    } catch (e: Exception) {
        null
    }
}

fun OpenIDConnectClient(
    discoveryUri: String? = null,
    block: OpenIDConnectClientConfig.() -> Unit
): OpenIDConnectClient {
    val config = OpenIDConnectClientConfig(discoveryUri).apply(block)
    return OpenIDConnectClient(config = config)
}

