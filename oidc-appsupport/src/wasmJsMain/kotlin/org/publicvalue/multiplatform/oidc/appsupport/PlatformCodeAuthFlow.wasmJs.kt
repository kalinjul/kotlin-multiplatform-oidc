package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

@ExperimentalOpenIdConnect
actual class PlatformCodeAuthFlow(
    windowTarget: String = "",
    windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    redirectOrigin: String,
    actual override val client: OpenIdConnectClient,
    actual override val preferences: Preferences,
) : CodeAuthFlow, EndSessionFlow {

    private val webFlow = WebPopupFlow(windowTarget, windowFeatures, redirectOrigin, preferences)

    @ExperimentalOpenIdConnect
    actual override suspend fun startLoginFlow(request: AuthCodeRequest) {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())
        throwAuthenticationIfCancelled(result)
    }

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

