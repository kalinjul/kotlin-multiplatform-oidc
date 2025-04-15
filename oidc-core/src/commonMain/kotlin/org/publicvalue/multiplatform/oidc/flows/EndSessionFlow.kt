package org.publicvalue.multiplatform.oidc.flows

import io.ktor.http.URLBuilder
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
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
        if (!client.config.discoveryUri.isNullOrEmpty()) {
            client.discover()
        }
        val request = client.createEndSessionRequest(idToken, configureEndSessionUrl)
        endSession(request)
    }

    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun endSession(request: EndSessionRequest): EndSessionResponse
}