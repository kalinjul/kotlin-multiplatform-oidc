package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
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
        throwIfCancelled(result)
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse {
        // TODO persist endsession request and handle redirect intent accordingly
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())

        return if (result is WebAuthenticationFlowResult.Success) {
            when (val error = getErrorResult<Unit>(result.responseUri)) {
                null -> {
                    return EndSessionResponse.success(Unit)
                }
                else -> {
                    return error
                }
            }
        } else {
            // browser closed, no redirect
            EndSessionResponse.failure(OpenIdConnectException.AuthenticationCancelled("Logout cancelled"))
        }
    }
}
