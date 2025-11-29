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
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException

abstract class PreferencesCodeAuthFlow(
    val client: OpenIdConnectClient,
    val preferences: Preferences,
) : CodeAuthFlow {

    /**
     * Uses the request URL to open a browser and perform authorization.
     * Should return the Authorization Code.
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    @Suppress("unused")
    suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        return try {
            preferences.setAuthRequest(request)
            startLoginFlow(request)
            val (_, responseUri) = getResultFromPreferences()
            AuthCodeResponse.success(responseUri.toAuthCodeResult())
        } catch (e: Exception) {
            AuthCodeResponse.failure(e)
        }
    }

    override suspend fun startLogin(configureAuthUrl: (URLBuilder.() -> Unit)?): AuthCodeRequest = wrapExceptions {
        if (!client.config.discoveryUri.isNullOrEmpty()) {
            client.discover()
        }
        val request = client.createAuthorizationCodeRequest(configureAuthUrl)
        preferences.setAuthRequest(request)
        startLoginFlow(request)
        return request
    }

    /**
     * Uses the request URL to open a browser and perform authorization.
     * Call [continueLogin] after returning to your app to receive tokens.
     *
     * @param request The request containing the url and relevant state information
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    protected abstract suspend fun startLoginFlow(request: AuthCodeRequest)

    override suspend fun canContinueLogin(): Boolean {
        return preferences.getAuthRequest() != null && preferences.getResponseUri() != null
    }

    @Throws(CancellationException::class, OpenIdConnectException::class)
    override suspend fun continueLogin(configureTokenExchange: (HttpRequestBuilder.() -> Unit)?): AccessTokenResponse {
        val (authRequest, responseUri) = getResultFromPreferences()
        val tokenResponse = client.continueLogin(authRequest,  responseUri, configureTokenExchange)
        return tokenResponse
    }

    private suspend fun getResultFromPreferences(): Pair<AuthCodeRequest, Url> {
        val authRequest = preferences.getAuthRequest()
        val responseUri = preferences.getResponseUri()
        if (authRequest == null) {
            throw OpenIdConnectException.AuthenticationFailure("No authRequest present")
        }
        if (responseUri == null) {
            throw OpenIdConnectException.AuthenticationFailure("No responseUri present")
        }
        preferences.clearOidcPreferences()
        return authRequest to responseUri
    }
}