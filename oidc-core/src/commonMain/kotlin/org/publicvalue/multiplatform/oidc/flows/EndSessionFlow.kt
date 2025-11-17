package org.publicvalue.multiplatform.oidc.flows

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.getError
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.clearOidcPreferences
import org.publicvalue.multiplatform.oidc.preferences.getEndsessionRequest
import org.publicvalue.multiplatform.oidc.preferences.getResponseUri
import org.publicvalue.multiplatform.oidc.preferences.setEndSessionRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException

/**
 * Flow that implements endSession call via HTTP Get Request in a Browser.
 * Uses the [postLogoutRedirectUri][org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig.postLogoutRedirectUri]
 * to request redirection after logout to return to the app.
 */
interface EndSessionFlow {
    val client: OpenIdConnectClient
    val preferences: Preferences

    /**
     * End session using a GET-Request in a WebView.
     * This supports redirecting to the app after logout if post_logout_redirect_uri is set.
     *
     * @param idToken used for id_token_hint, recommended by openid spec, optional
     * @param configureEndSessionUrl configuration closure to configure the http request builder with
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun endSession(
        idToken: String?,
        configureEndSessionUrl: (URLBuilder.() -> Unit)? = null,
    ) = wrapExceptions {
        startLogout(idToken = idToken, configureEndSessionUrl = configureEndSessionUrl)
        continueLogout()
    }

    /**
     * Start end session flow using a GET-Request in a WebView.
     * This supports redirecting to the app after logout if post_logout_redirect_uri is set.
     *
     * Call [continueLogout] after returning to your app to check for errors during logout.
     *
     * @param idToken used for id_token_hint, recommended by openid spec, optional
     * @param configureEndSessionUrl configuration closure to configure the http request builder with
     */
    suspend fun startLogout(
        idToken: String?,
        configureEndSessionUrl: (URLBuilder.() -> Unit)? = null,
    ) = wrapExceptions {
        if (!client.config.discoveryUri.isNullOrEmpty()) {
            client.discover()
        }
        val request = client.createEndSessionRequest(idToken, configureEndSessionUrl)
        preferences.setEndSessionRequest(request)
        startLogoutFlow(request)
    }

    /**
     * Start end session flow using a GET-Request in a WebView.
     * This supports redirecting to the app after logout if post_logout_redirect_uri is set.
     *
     * Call [continueLogout] after returning to your app to check for errors during logout.
     *
     * @param request The request containing the url with relevant parameters
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun startLogoutFlow(request: EndSessionRequest)

    /**
     * Check whether continueLogout can safely be called.
     *
     * @return true if startLogout() was called before and continueLogout() was not yet called.
     */
    suspend fun canContinueLogout(): Boolean {
        return preferences.getEndsessionRequest() != null && preferences.getResponseUri() != null
    }

    /**
     * Continue logout flow.
     *
     * @throws OpenIdConnectException if canContinueLogout() returns false or if there was an error during logout.
     *
     */
    suspend fun continueLogout() {
        val endSessionRequest = preferences.getEndsessionRequest()
        val responseUri = preferences.getResponseUri()
        if (endSessionRequest == null) {
            throw OpenIdConnectException.AuthenticationFailure("No endSessionRequest present")
        }
        if (responseUri == null) {
            throw OpenIdConnectException.AuthenticationFailure("No responseUri present")
        }
        preferences.clearOidcPreferences()
        client.continueLogout(responseUri)
    }
}

@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun OpenIdConnectClient.continueLogout(
    responseUri: Url,
) {
    responseUri.getError()?.let { throw it }
}