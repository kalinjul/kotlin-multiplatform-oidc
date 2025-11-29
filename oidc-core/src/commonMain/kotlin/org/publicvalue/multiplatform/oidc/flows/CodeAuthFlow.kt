package org.publicvalue.multiplatform.oidc.flows

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.clearOidcPreferences
import org.publicvalue.multiplatform.oidc.preferences.getAuthRequest
import org.publicvalue.multiplatform.oidc.preferences.getResponseUri
import org.publicvalue.multiplatform.oidc.preferences.setAuthRequest
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.parseJwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.validateNonce
import org.publicvalue.multiplatform.oidc.types.validateState
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

    /**
     * Start the authorization flow to request an access token.
     *
     * This may not return in some cases on Android if the application is terminated while the login website is shown.
     * In this cases, call continueLogin() manually after your application restarts.
     *
     * @return the AccessTokenResponse.
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
        startLogin(configureAuthUrl)
        return continueLogin(configureTokenExchange)
    }

    /**
     * Start the authorization flow.
     *
     * @return the Authorization Code Request
     *
     * @param configureAuthUrl configuration closure to configure the auth url passed to browser
     */
    @Suppress("unused")
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun startLogin(
        configureAuthUrl: (URLBuilder.() -> Unit)? = null,
    ): AuthCodeRequest

    /**
     * Check whether continueLogin can safely be called.
     *
     * @return true if startLogin() was called before and continueLogin() was not yet called.
     */
    suspend fun canContinueLogin(): Boolean

    /**
     * Continue login flow.
     *
     * @throws OpenIdConnectException if canContinueLogin() returns false or if token exchange fails.
     *
     * @param configureTokenExchange configuration closure to configure the http request builder with (will _not_
     * be used for discovery if necessary)
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun continueLogin(configureTokenExchange: (HttpRequestBuilder.() -> Unit)? = null): AccessTokenResponse
}


/**
 * Continue login flow.
 *
 * @param request The original auth code request
 * @param responseUri URI returned by the IDP in response to the authorization, containing code and state.
 * @param configureTokenExchange configuration closure to configure the http request builder with (will _not_
 * be used for discovery if necessary)
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun OpenIdConnectClient.continueLogin(
    request: AuthCodeRequest,
    responseUri: Url,
    configureTokenExchange: (HttpRequestBuilder.() -> Unit)? = null
): AccessTokenResponse {
    return continueLogin(
        request = request,
        result = responseUri.toAuthCodeResult(),
        configureTokenExchange = configureTokenExchange
    )
}

/**
 * Continue login flow.
 *
 * @param request The original auth code request
 * @param result [AuthCodeResult] containing the authorization code and state returned by the IDP
 * @param configureTokenExchange Configuration closure to configure the http request builder with (will _not_
 * be used for discovery if necessary)
 *
 * @return The AccessTokenResponse
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
private suspend fun OpenIdConnectClient.continueLogin(
    request: AuthCodeRequest,
    result: AuthCodeResult,
    configureTokenExchange: (HttpRequestBuilder.() -> Unit)?
): AccessTokenResponse {
    if (result.code != null) {
        if (!request.validateState(result.state ?: "")) {
            throw OpenIdConnectException.AuthenticationFailure("Invalid state")
        }
        val response = exchangeToken(request, result.code, configureTokenExchange)
        val nonce = response.id_token?.parseJwt()?.payload?.nonce
        if (!request.validateNonce(nonce ?: "")) {
            throw OpenIdConnectException.AuthenticationFailure("Invalid nonce")
        }
        return response
    } else {
        throw OpenIdConnectException.AuthenticationFailure("No auth code", cause = null)
    }
}