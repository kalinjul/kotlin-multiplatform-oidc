package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

public actual class PlatformCodeAuthFlow internal constructor(
    private val webFlow: WebAuthenticationFlow,
    actual override val client: OpenIdConnectClient,
) : CodeAuthFlow, EndSessionFlow {

    // TODO extract common code
    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        val result =
            webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())

        return if (result is WebAuthenticationFlowResult.Success) {
            when (val error = getErrorResult<AuthCodeResult>(result.responseUri)) {
                null -> {
                    val state = result.responseUri.parameters.get("state")
                    val code = result.responseUri.parameters.get("code")
                    Result.success(AuthCodeResult(code, state))
                }

                else -> {
                    return error
                }
            }
        } else {
            // browser closed, no redirect
            Result.failure(OpenIdConnectException.AuthenticationCancelled())
        }
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse {
        val result = webFlow.startWebFlow(
            request.url,
            request.url.parameters.get("post_logout_redirect_uri").orEmpty()
        )

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
