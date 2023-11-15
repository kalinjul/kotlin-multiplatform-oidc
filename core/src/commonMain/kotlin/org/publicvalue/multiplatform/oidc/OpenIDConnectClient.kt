package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.flows.PKCE
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

class OpenIDConnectClient(
    val httpClient: HttpClient = HttpClient(),
    val config: OpenIDClientConfig,
) {
    fun createAuthCodeRequest(): AuthCodeRequest {
//        config.validate() // TODO
        val pkce = PKCE(config.codeChallengeMethod)
        val nonce = randomBytes().encodeForPKCE()
        val state = randomBytes().encodeForPKCE()

        val url = URLBuilder(config.endpoints?.authorizationEndpoint!!).apply {
            parameters.append("client_id", config.clientId!!)
            parameters.append("client_secret", config.clientSecret!!)
            parameters.append("response_type", "code")
            parameters.append("response_mode", "query")
            parameters.append("scope", config.scope!!)
            parameters.append("nonce", nonce)
            parameters.append("code_challenge_method", config.codeChallengeMethod.name)
            parameters.append("code_challenge", pkce.codeChallenge)
            parameters.append("redirect_uri", config.redirectUri ?: "")
//            parameters.append("state", currentState)
        }.build()

        println(url)

        return AuthCodeRequest(
            url, config, pkce, state, nonce
        )
    }

    fun exchangeToken(authCodeRequest: AuthCodeRequest, code: String) {
        // send code_verifier to server
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
    var codeChallengeMethod: CodeChallengeMethod = CodeChallengeMethod.S256,
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

    fun clientSecret(clientSecret: String) { // TODO remove
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