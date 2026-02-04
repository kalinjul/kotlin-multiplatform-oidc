package org.publicvalue.multiplatform.oidc.flows

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.clearOidcPreferences
import org.publicvalue.multiplatform.oidc.preferences.getEndsessionRequest
import org.publicvalue.multiplatform.oidc.preferences.getResponseUri
import org.publicvalue.multiplatform.oidc.preferences.setEndSessionRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException

abstract class PreferencesEndSessionFlow(
    val client: OpenIdConnectClient,
    val preferences: Preferences
): EndSessionFlow {

    @Throws(CancellationException::class, OpenIdConnectException::class)
    override suspend fun startLogout(
        idToken: String?,
        configureEndSessionUrl: (URLBuilder.() -> Unit)?
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
    protected abstract suspend fun startLogoutFlow(request: EndSessionRequest)

    override suspend fun canContinueLogout(): Boolean {
        return preferences.getEndsessionRequest() != null && preferences.getResponseUri() != null
    }

    @Throws(CancellationException::class, OpenIdConnectException::class)
    override suspend fun continueLogout() {
        val (_, responseUri) = getResultFromPreferences()
        client.continueLogout(responseUri)
    }

    private suspend fun getResultFromPreferences(): Pair<EndSessionRequest, Url> {
        val endSessionRequest = preferences.getEndsessionRequest()
        val responseUri = preferences.getResponseUri()
        if (endSessionRequest == null) {
            throw OpenIdConnectException.AuthenticationFailure("No endSessionRequest present")
        }
        if (responseUri == null) {
            throw OpenIdConnectException.AuthenticationFailure("No responseUri present")
        }
        preferences.clearOidcPreferences()
        return endSessionRequest to responseUri
    }
}