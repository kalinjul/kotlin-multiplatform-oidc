package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.PreferencesCodeAuthFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

actual class PlatformCodeAuthFlow internal constructor(
    private val webFlow: WebAuthenticationFlow,
    client: OpenIdConnectClient,
    preferences: Preferences
) : PreferencesCodeAuthFlow(client, preferences) {

    actual override suspend fun startLoginFlow(request: AuthCodeRequest) {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())
        throwAuthenticationIfCancelled(result)
    }
}
