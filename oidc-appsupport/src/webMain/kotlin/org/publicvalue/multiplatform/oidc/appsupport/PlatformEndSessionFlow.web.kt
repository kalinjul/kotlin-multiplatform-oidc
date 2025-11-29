package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.PreferencesEndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

@ExperimentalOpenIdConnect
actual class PlatformEndSessionFlow(
    windowTarget: String = "",
    windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    redirectOrigin: String,
    client: OpenIdConnectClient,
    preferences: Preferences,
) : PreferencesEndSessionFlow(client, preferences) {

    private val webFlow = WebPopupFlow(windowTarget, windowFeatures, redirectOrigin, preferences)

    actual override suspend fun startLogoutFlow(request: EndSessionRequest) {
        val redirectUrl = request.url.parameters.get("post_logout_redirect_uri").orEmpty()
        val result = webFlow.startWebFlow(request.url, redirectUrl)
        throwEndsessionIfCancelled(result)
    }

    companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            WebPopupFlow.handleRedirect()
        }
    }
}

