package org.publicvalue.multiplatform.oidc.flows

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.getError
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException

/**
 * Flow that implements endSession call via HTTP Get Request in a Browser.
 * Uses the [postLogoutRedirectUri][org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig.postLogoutRedirectUri]
 * to request redirection after logout to return to the app.
 */
interface EndSessionFlow {

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
    )



    /**
     * Check whether continueLogout can safely be called.
     *
     * @return true if startLogout() was called before and continueLogout() was not yet called.
     */
    suspend fun canContinueLogout(): Boolean

    /**
     * Continue logout flow.
     *
     * @throws OpenIdConnectException if canContinueLogout() returns false or if there was an error during logout.
     *
     */
    suspend fun continueLogout()
}

@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun OpenIdConnectClient.continueLogout(
    responseUri: Url,
) {
    responseUri.getError()?.let { throw it }
}