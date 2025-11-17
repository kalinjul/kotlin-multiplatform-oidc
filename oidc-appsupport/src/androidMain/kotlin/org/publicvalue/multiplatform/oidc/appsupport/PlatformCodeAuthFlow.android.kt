package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

actual class PlatformCodeAuthFlow internal constructor(
    private val webFlow: WebAuthenticationFlow,
    actual override val client: OpenIdConnectClient,
    actual override val preferences: Preferences
) : CodeAuthFlow, EndSessionFlow {

    actual override suspend fun startLoginFlow(request: AuthCodeRequest) {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())
        throwAuthenticationIfCancelled(result)
    }

    actual override suspend fun startLogoutFlow(request: EndSessionRequest) {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())
        throwEndsessionIfCancelled(result)
    }
}
