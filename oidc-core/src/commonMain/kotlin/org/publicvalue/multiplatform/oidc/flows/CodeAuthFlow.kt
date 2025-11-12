package org.publicvalue.multiplatform.oidc.flows

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.parseJwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.validateNonce
import org.publicvalue.multiplatform.oidc.types.validateState
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1)
 *
 * Implementations have to provide their own method [startLoginFlow]
 * as this requires user interaction (e.g. via browser).
 */

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AbstractCodeAuthFlow", name = "AbstractCodeAuthFlow", exact = true)
interface CodeAuthFlow {
    val client: OpenIdConnectClient

    /**
     * Start the authorization flow to request an access token.
     *
     * @param configureAuthUrl configuration closure to configure the auth url passed to browser
     * @param configureTokenExchange configuration closure to configure the http request builder with (will _not_
     * be used for discovery if necessary)
     */
    @Suppress("unused")
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun getAccessToken(
        configureAuthUrl: (URLBuilder.() -> Unit)? = null,
        configureTokenExchange: (HttpRequestBuilder.() -> Unit)? = null
    ): AccessTokenResponse {
        startLogin()
        return client.continueLogin( null)
    }

    /**
     * Start the authorization flow.
     *
     * @param configureAuthUrl configuration closure to configure the auth url passed to browser
     */
    @Suppress("unused")
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun startLogin(
        configureAuthUrl: (URLBuilder.() -> Unit)? = null,
    ) = wrapExceptions {
        if (!client.config.discoveryUri.isNullOrEmpty()) {
            client.discover()
        }
        val request = client.createAuthorizationCodeRequest(configureAuthUrl)
        Preferences.lastRequest = request
        startLoginFlow(request)
    }

    /**
     * Uses the request URL to open a browser and perform authorization.
     * @param request The request containing the url and relevant state information
     * @return the Authorization Code.
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun startLoginFlow(request: AuthCodeRequest)
}

/**
 * Continue login flow.
 *
 * @param configureTokenExchange configuration closure to configure the http request builder with (will _not_
 * be used for discovery if necessary)
 */
suspend fun OpenIdConnectClient.continueLogin(configureTokenExchange: (HttpRequestBuilder.() -> Unit)? = null): AccessTokenResponse {
    val parsed = Preferences.resultUri!!
    getError(parsed)?.let { throw it }

    val state = parsed.parameters["state"]
    val code = parsed.parameters["code"]

    val request: AuthCodeRequest = Preferences.lastRequest!!
    return exchangeToken(
        request = request,
        result = AuthCodeResult(code, state),
        configure = configureTokenExchange
    ).also {
        Preferences.lastRequest = null
        Preferences.resultUri = null
    }
}

private fun getError(responseUri: Url?): OpenIdConnectException.AuthenticationFailure? {
    return if (responseUri?.parameters?.contains("error") == true) {
        OpenIdConnectException.AuthenticationFailure(
            message = responseUri.parameters.get("error") ?: "")
    } else null
}

private suspend fun OpenIdConnectClient.exchangeToken(
    request: AuthCodeRequest,
    result: AuthCodeResult,
    configure: (HttpRequestBuilder.() -> Unit)?
): AccessTokenResponse {
    if (result.code != null) {
        if (!request.validateState(result.state ?: "")) {
            throw OpenIdConnectException.AuthenticationFailure("Invalid state")
        }
        val response = exchangeToken(request, result.code, configure)
        val nonce = response.id_token?.parseJwt()?.payload?.nonce
        if (!request.validateNonce(nonce ?: "")) {
            throw OpenIdConnectException.AuthenticationFailure("Invalid nonce")
        }
        return response
    } else {
        throw OpenIdConnectException.AuthenticationFailure("No auth code", cause = null)
    }
}