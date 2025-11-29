package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.PreferencesCodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.PreferencesEndSessionFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

expect class PlatformCodeAuthFlow: PreferencesCodeAuthFlow {
    override suspend fun startLoginFlow(request: AuthCodeRequest)
}

expect class PlatformEndSessionFlow: PreferencesEndSessionFlow {
    override suspend fun startLogoutFlow(request: EndSessionRequest)
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