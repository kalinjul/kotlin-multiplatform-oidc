package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

expect class PlatformCodeAuthFlow: CodeAuthFlow, EndSessionFlow {
    override suspend fun startLoginFlow(request: AuthCodeRequest)
    override suspend fun startLogoutFlow(request: EndSessionRequest)
    override val client: OpenIdConnectClient
    override val preferences: Preferences
}

internal fun throwAuthenticationIfCancelled(result: WebAuthenticationFlowResult) {
    if (result is WebAuthenticationFlowResult.Cancelled) {
        throw OpenIdConnectException.AuthenticationCancelled()
    }
}

internal fun throwEndsessionIfCancelled(result: WebAuthenticationFlowResult) {
    if (result is WebAuthenticationFlowResult.Cancelled) {
        throw OpenIdConnectException.AuthenticationCancelled("Logout Cancelled")
    }
}