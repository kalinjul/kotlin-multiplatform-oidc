package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.prepareForm
import io.ktor.client.request.url
import io.ktor.client.statement.HttpStatement
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.http.parseUrlEncodedParameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.flows.PKCE
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

@OptIn(ExperimentalSerializationApi::class)
class OpenIDConnectClient(
    val httpClient: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                explicitNulls = false
                ignoreUnknownKeys = true
            })
        }
    },
    val config: OpenIDClientConfig,
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
            parameters.append("code_challenge_method", config.codeChallengeMethod.name)
            if (config.codeChallengeMethod != CodeChallengeMethod.off) { parameters.append("code_challenge", pkce.codeChallenge) }
            config.redirectUri?.let { parameters.append("redirect_uri", it) }
            parameters.append("state", state)
        }.build()

        println(url)

        return AuthCodeRequest(
            url, config, pkce, state, nonce
        )
    }

    suspend fun parseAuthCode(request: AuthCodeRequest, queryParameters: String): String? {
        val parameters = queryParameters.parseUrlEncodedParameters()
        val code = parameters["code"]
        val state = parameters["state"]
        if (state != request.state) {
            throw OpenIDConnectException.AuthenticationFailed("State in response does not match request state: $queryParameters")
        }
        return code
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3
     * + code verifier https://datatracker.ietf.org/doc/html/rfc7636#section-4.5
     */
    suspend fun exchangeTokenRequest(authCodeRequest: AuthCodeRequest, code: String): AccessTokenResponse? {
        // send code_verifier to server
        val (httpFormRequest, params) = createExchangeTokenRequest(authCodeRequest, code)

        println("exchange token params: $params")
        val response = httpFormRequest.execute()

        return if (response.status.isSuccess()) {
            val body = response.call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
            if (body.startsWith("error")) {
                throw OpenIDConnectException.UnsuccessfulTokenRequest("Exchange token failed: ${response.status.value} $body", response.status, body)
            } else {
                val accessTokenResponse: AccessTokenResponse? = response.call.body()
                println(accessTokenResponse)
                accessTokenResponse
            }

        } else {
            val body = response.call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
            println(body)
            throw OpenIDConnectException.UnsuccessfulTokenRequest("Exchange token failed: ${response.status.value} $body", response.status, body)
        }
    }

    suspend fun createExchangeTokenRequest(authCodeRequest: AuthCodeRequest, code: String): Pair<HttpStatement, Parameters> {
        val url = URLBuilder(config.endpoints?.tokenEndpoint!!).build()

        println("using token url: $url")

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
        return request to formParameters
    }
}

data class AuthCodeRequest(
    val url: Url,
    val config: OpenIDClientConfig,
    val pkce: PKCE,
    val state: String,
    val nonce: String
)

//fun OpenIDConnectConfiguration.getTokenRequestUrl(): Url =
//    if (token_endpoint != null) {
//        try {
//            Url(token_endpoint)
//        } catch (e: URLParserException) {
//            throw OpenIDConnectException.InvalidUrl(token_endpoint, e)
//        }
//    } else {
//        throw OpenIDConnectException.InvalidUrl(token_endpoint)
//    }

fun OpenIDConnectClient(
    block: OpenIDClientConfig.() -> Unit
): OpenIDConnectClient {
    val config = OpenIDClientConfig().apply(block)
    return OpenIDConnectClient(config = config)
}

@EndpointMarker
class OpenIDClientConfig(
    var endpoints: Endpoints? = null,
    var clientId: String? = null,
    var clientSecret: String? = null, // TODO remove
    var scope: String? = null,
    var codeChallengeMethod: CodeChallengeMethod = CodeChallengeMethod.off,
    var redirectUri: String? = null,
) {
    fun endpoints(
        block: Endpoints.() -> Unit
    ) {
        this.endpoints = Endpoints().apply(block)
    }

    /**
     * REQUIRED
     * https://datatracker.ietf.org/doc/html/rfc6749#section-2.2
     */
    fun clientId(clientId: String) {
        this.clientId = clientId
    }

    fun clientSecret(clientSecret: String) {
        this.clientSecret = clientSecret
    }

    /**
     * OPTIONAL
     * https://datatracker.ietf.org/doc/html/rfc6749#section-3.3
     */
    fun scope(scope: String) {
        this.scope = scope
    }

    fun codeChallengeMethod(codeChallengeMethod: CodeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod
    }

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-3.1.2
     */
    fun redirectUri(redirectUri: String) {
        this.redirectUri = redirectUri
    }
}

@DslMarker
annotation class EndpointMarker

@EndpointMarker
data class Endpoints(
    var tokenEndpoint: String? = null,
    var authorizationEndpoint: String? = null,
    var userInfoEndpoint: String? = null
) {
    fun baseUrl(baseUrl: String, block: Endpoints.() -> Unit) {
        val endpoints = Endpoints()
        endpoints.block()
        tokenEndpoint = baseUrl + endpoints.tokenEndpoint
        authorizationEndpoint = baseUrl + endpoints.authorizationEndpoint
//        return endpoints.copy(
//            tokenEndpoint = baseUrl + tokenEndpoint,
//            authorizationEndpoint = baseUrl + authorizationEndpoint
//        )
    }

    fun tokenEndpoint(tokenEndpoint: String) {
        this.tokenEndpoint = tokenEndpoint
    }
    fun authEndpoint(authorizationEndpoint: String) {
        this.authorizationEndpoint = authorizationEndpoint
    }
    fun userInfoEndpoint(userInfoEndpoint: String) {
        this.userInfoEndpoint = userInfoEndpoint
    }
}