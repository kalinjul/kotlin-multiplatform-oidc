package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.prepareForm
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import io.ktor.http.ContentType
import io.ktor.http.ContentTypeMatcher
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.flows.PKCE
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.TokenRequest

@OptIn(ExperimentalSerializationApi::class)
class OpenIDConnectClient(
    val httpClient: HttpClient = HttpClient {
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
    },
    val config: OpenIDConnectClientConfig,
) {
    init {
//        config.validate() // TODO
        if (config.endpoints?.tokenEndpoint == null) {
            throw OpenIDConnectException.InvalidUrl("Invalid configuration: tokenEndpoint is null")
        }
        if (config.endpoints?.authorizationEndpoint == null) {
            throw OpenIDConnectException.InvalidUrl("Invalid configuration: authorizationEndpoint is null")
        }

        if (config.clientId == null) {
            throw OpenIDConnectException.InvalidUrl("Invalid configuration: clientId is null")
        }
    }

    fun createAuthCodeRequest(): AuthCodeRequest {
        val pkce = PKCE(config.codeChallengeMethod)
        val nonce = randomBytes().encodeForPKCE()
        val state = randomBytes().encodeForPKCE()

        val url = URLBuilder(config.endpoints?.authorizationEndpoint!!).apply {
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

    /**
     * https://openid.net/specs/openid-connect-rpinitiated-1_0.html
     */
    suspend fun endSession(tokens: AccessTokenResponse): HttpStatusCode {
        config.endpoints?.endSessionEndpoint?.let {
            val url = URLBuilder(it)
            val response = httpClient.post {
                this.url(url.build())
                parameter("id_token_hint", tokens.id_token)
            }
            return response.status
        } ?: run {
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
        val url = URLBuilder(config.endpoints?.tokenEndpoint!!).build()

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
            val body = response.call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
            if (body.startsWith("error")) {
                throw OpenIDConnectException.UnsuccessfulTokenRequest(
                    "Exchange token failed: ${response.status.value} $body",
                    response.status,
                    body
                )
            } else {
                try {
                    val accessTokenResponse: AccessTokenResponse = response.call.body()
                    accessTokenResponse
                } catch (e: NoTransformationFoundException) {
                    throw OpenIDConnectException.UnsuccessfulTokenRequest(
                        message = "Could not decode response",
                        cause = e,
                        statusCode = response.status,
                        body = response.call.body<String>()
                    )
                }
            }

        } else {
            val body = response.call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
            throw OpenIDConnectException.UnsuccessfulTokenRequest(
                "Exchange token failed: ${response.status.value} $body",
                response.status,
                body
            )
        }
    }
}

fun OpenIDConnectClient(
    block: OpenIDConnectClientConfig.() -> Unit
): OpenIDConnectClient {
    val config = OpenIDConnectClientConfig().apply(block)
    return OpenIDConnectClient(config = config)
}

