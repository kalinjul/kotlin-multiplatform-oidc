package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.PreferencesEndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.experimental.ExperimentalObjCName

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1
 *
 * Implementations have to provide their own method to get the authorization code,
 * as this requires user interaction (e.g. via browser).
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "CodeAuthFlow", name = "CodeAuthFlow", exact = true)
actual class PlatformEndSessionFlow internal constructor(
    client: OpenIdConnectClient,
    ephemeralBrowserSession: Boolean = false,
    preferences: Preferences,
    private val webFlow: WebAuthenticationFlow,
): PreferencesEndSessionFlow(client, preferences) {

    actual override suspend fun startLogoutFlow(request: EndSessionRequest) = wrapExceptions {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())
        throwEndsessionIfCancelled(result)
    }
}
