package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.PreferencesCodeAuthFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

@ExperimentalOpenIdConnect
actual class PlatformCodeAuthFlow internal constructor(
    client: OpenIdConnectClient,
    preferences: Preferences,
    private val webFlow: WebAuthenticationFlow,
) : PreferencesCodeAuthFlow(client, preferences) {

    actual override suspend fun startLoginFlow(request: AuthCodeRequest) {
        val redirectUrl = request.url.parameters.get("redirect_uri").orEmpty()
        checkRedirectPort(Url(redirectUrl))
        val result = webFlow.startWebFlow(request.url, redirectUrl)
        throwAuthenticationIfCancelled(result)
    }
}