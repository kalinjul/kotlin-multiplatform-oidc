package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.prepareForm
import io.ktor.client.request.url
import io.ktor.http.URLBuilder
import io.ktor.http.decodeURLQueryComponent
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.flows.PKCE
import org.publicvalue.multiplatform.oidc.types.AccessTokenRequest
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
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

    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3
     * + code verifier https://datatracker.ietf.org/doc/html/rfc7636#section-4.5
     */
    suspend fun exchangeToken(authCodeRequest: AuthCodeRequest, code: String): AccessTokenResponse {
        // send code_verifier to server
        val (httpFormRequest, params) = createAccessTokenRequest(authCodeRequest, code)

        val response = httpFormRequest.execute()

        return if (response.status.isSuccess()) {
            val body = response.call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
            if (body.startsWith("error")) {
                throw OpenIDConnectException.UnsuccessfulTokenRequest("Exchange token failed: ${response.status.value} $body", response.status, body)
            } else {
                val accessTokenResponse: AccessTokenResponse = response.call.body()
                accessTokenResponse
            }

        } else {
            val body = response.call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
            throw OpenIDConnectException.UnsuccessfulTokenRequest("Exchange token failed: ${response.status.value} $body", response.status, body)
        }
    }

    suspend fun createAccessTokenRequest(authCodeRequest: AuthCodeRequest, code: String): AccessTokenRequest {
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
        return AccessTokenRequest(
            request,
            formParameters
        )
    }
}

fun OpenIDConnectClient(
    block: OpenIDConnectClientConfig.() -> Unit
): OpenIDConnectClient {
    val config = OpenIDConnectClientConfig().apply(block)
    return OpenIDConnectClient(config = config)
}

