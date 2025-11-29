package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.PreferencesEndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

@ExperimentalOpenIdConnect
actual class PlatformEndSessionFlow internal constructor(
    client: OpenIdConnectClient,
    preferences: Preferences,
    private val webFlow: WebPopupFlow,
) : PreferencesEndSessionFlow(client, preferences) {

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

