package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.prepareForm
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.parameter
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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.discovery.OpenIdConnectDiscover
import org.publicvalue.multiplatform.oidc.flows.Pkce
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.TokenRequest
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import org.publicvalue.multiplatform.oidc.types.remote.OpenIdConnectConfiguration
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * OpenIdConnectClient implements the basic methods used to perform OpenID Connect Authentication.
 * A client may also be constructed using the [Builder method][org.publicvalue.multiplatform.oidc.OpenIdConnectClient]
 *
 * @param httpClient The (ktor) HTTP client to be used for code <-> token exchange and endSession requests.
 * Authentication is performed using [CodeAuthFlow][org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow]
 *
 * @param config [Configuration][org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig] for this client
 */
@OptIn(ExperimentalSerializationApi::class, ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIdConnectClient", name = "OpenIdConnectClient", exact = true)
class DefaultOpenIdConnectClient(
    private val httpClient: HttpClient = DefaultHttpClient,
    override val config: OpenIdConnectClientConfig,
) : OpenIdConnectClient {
    /**
     * Swift convenience constructor
     * @suppress
     */
    constructor(config: OpenIdConnectClientConfig): this(httpClient = DefaultHttpClient, config = config)

    override var discoverDocument: OpenIdConnectConfiguration? = null

    companion object {
        val DefaultHttpClient by lazy {
            HttpClient {
                install(ContentNegotiation) {
                    // register custom type matcher to support broken IDPs that don't send correct content-type
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

    @Throws(OpenIdConnectException::class)
    override fun createAuthorizationCodeRequest(configure: (URLBuilder.() -> Unit)?): AuthCodeRequest {
        val pkce = Pkce(config.codeChallengeMethod)
        val nonce = secureRandomBytes().encodeForPKCE()
        val state = secureRandomBytes().encodeForPKCE()

        val authorizationEndpoint = config.endpoints?.authorizationEndpoint ?: run { throw OpenIdConnectException.InvalidConfiguration("No authorizationEndpoint set") }
        val url = URLBuilder(authorizationEndpoint).apply {
            parameters.append("client_id", config.clientId!!)
            parameters.append("response_type", "code")
            parameters.append("response_mode", "query")
            config.scope?.let { parameters.append("scope", it) }
            parameters.append("nonce", nonce)
            config.codeChallengeMethod.queryString?.let { parameters.append("code_challenge_method", it) }
            if (config.codeChallengeMethod != CodeChallengeMethod.off) { parameters.append("code_challenge", pkce.codeChallenge) }
            config.redirectUri?.let { parameters.append("redirect_uri", it) }
            parameters.append("state", state)
            configure?.invoke(this)
        }.build()

        return AuthCodeRequest(
            url, config, pkce, state, nonce
        )
    }

    @Throws(OpenIdConnectException::class, CancellationException::class)
    override suspend fun discover(configure: (HttpRequestBuilder.() -> Unit)?) = wrapExceptions {
        config.discoveryUri?.let { discoveryUri ->
            val config = OpenIdConnectDiscover(httpClient).downloadConfiguration(discoveryUri, configure)
            this.config.updateWithDiscovery(config)
            discoverDocument = config
        } ?: run {
            throw OpenIdConnectException.InvalidUrl("No discoveryUri set")
        }
    }

    @Throws(OpenIdConnectException::class, CancellationException::class)
    override suspend fun endSession(idToken: String, configure: (HttpRequestBuilder.() -> Unit)?): HttpStatusCode = wrapExceptions {
        val endpoint = config.endpoints?.endSessionEndpoint?.trim()
        if (!endpoint.isNullOrEmpty()) {
            val url = URLBuilder(endpoint)
            val response = httpClient.submitForm {
                url(url.build())
                parameter("id_token_hint", idToken)
                configure?.invoke(this)
            }
            response.status
        } else {
            throw OpenIdConnectException.InvalidUrl("No endSessionEndpoint set")
        }
    }

    @Throws(OpenIdConnectException::class, CancellationException::class)
    override suspend fun exchangeToken(authCodeRequest: AuthCodeRequest, code: String, configure: (HttpRequestBuilder.() -> Unit)?): AccessTokenResponse = wrapExceptions {
        val tokenRequest = createAccessTokenRequest(authCodeRequest, code, configure)
        return executeTokenRequest(tokenRequest.request)
    }

    @Throws(OpenIdConnectException::class, CancellationException::class)
    @Suppress("Unused")
    override suspend fun refreshToken(refreshToken: String, configure: (HttpRequestBuilder.() -> Unit)?): AccessTokenResponse = wrapExceptions {
        val tokenRequest = createRefreshTokenRequest(refreshToken, configure)
        return executeTokenRequest(tokenRequest.request)
    }

    @Throws(OpenIdConnectException::class, CancellationException::class)
    override suspend fun createAccessTokenRequest(authCodeRequest: AuthCodeRequest, code: String, configure: (HttpRequestBuilder.() -> Unit)?): TokenRequest = wrapExceptions {
        val url = URLBuilder(getOrDiscoverTokenEndpoint()).build()

        val formParameters = parameters {
            append("grant_type", "authorization_code")
            append("code", code)
            config.redirectUri?.let { append("redirect_uri", it) }
            append("client_id", config.clientId ?: run { throw OpenIdConnectException.InvalidConfiguration("clientId is missing") })
            config.clientSecret?.let { append("client_secret", it) }
            if (config.codeChallengeMethod != CodeChallengeMethod.off) { append("code_verifier", authCodeRequest.pkce.codeVerifier) }
        }
        val request = runBlocking { // there is no suspending happening here
            httpClient.prepareForm(
                formParameters = formParameters
            ) {
                url(url)
                configure?.invoke(this)
            }
        }
        return TokenRequest(
            request,
            formParameters
        )
    }

    @Throws(OpenIdConnectException::class, CancellationException::class)
    override suspend fun createRefreshTokenRequest(refreshToken: String, configure: (HttpRequestBuilder.() -> Unit)?): TokenRequest = wrapExceptions {
        val url = URLBuilder(getOrDiscoverTokenEndpoint()).build()

        val formParameters = parameters {
            append("grant_type", "refresh_token")
            append("client_id", config.clientId ?: run { throw OpenIdConnectException.InvalidConfiguration("clientId is missing") })
            config.clientSecret?.let { append("client_secret", it) }
            append("refresh_token", refreshToken)
            config.scope?.let { append("scope", it) }
        }
        val request = runBlocking { // there is no suspending happening here
            httpClient.prepareForm(
                formParameters = formParameters
            ) {
                url(url)
                configure?.invoke(this)
            }
        }
        return TokenRequest(
            request,
            formParameters
        )
    }

    private suspend fun getOrDiscoverTokenEndpoint(): String {
        return if (config.endpoints?.tokenEndpoint != null) {
            config.endpoints?.tokenEndpoint!!
        } else {
            discover()
            config.endpoints?.tokenEndpoint!!
        }
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

    private suspend fun HttpResponse.toOpenIdConnectException(): OpenIdConnectException.UnsuccessfulTokenRequest {
        val errorResponse = call.errorBody()
        val body = call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
        return OpenIdConnectException.UnsuccessfulTokenRequest(
            message = "Exchange token failed: ${status.value} ${errorResponse?.error_description ?: errorResponse?.error}",
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