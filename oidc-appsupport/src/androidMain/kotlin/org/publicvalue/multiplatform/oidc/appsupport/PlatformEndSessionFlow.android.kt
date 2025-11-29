package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.PreferencesEndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

actual class PlatformEndSessionFlow internal constructor(
    private val webFlow: WebAuthenticationFlow,
    client: OpenIdConnectClient,
    preferences: Preferences
) : PreferencesEndSessionFlow(client, preferences) {

    actual override suspend fun startLogoutFlow(request: EndSessionRequest) {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())
        throwEndsessionIfCancelled(result)
    }
}
