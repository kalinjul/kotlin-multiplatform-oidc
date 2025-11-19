package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.*
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

@ExperimentalOpenIdConnect
actual class PlatformCodeAuthFlow(
    windowTarget: String = "",
    windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    redirectOrigin: String,
    actual override val client: OpenIdConnectClient,
) : CodeAuthFlow, EndSessionFlow {

    private val webFlow = WebPopupFlow(windowTarget, windowFeatures, redirectOrigin)

    @ExperimentalOpenIdConnect
    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())

        return if (result is WebAuthenticationFlowResult.Success) {
            when (val error = getErrorResult<AuthCodeResult>(result.responseUri)) {
                null -> {
                    val state = result.responseUri?.parameters?.get("state")
                    val code = result.responseUri?.parameters?.get("code")
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
        val redirectUrl = request.url.parameters.get("post_logout_redirect_uri").orEmpty()
        webFlow.startWebFlow(request.url, redirectUrl)
        return Result.success(Unit)
    }

    companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            WebPopupFlow.handleRedirect()
        }
    }
}