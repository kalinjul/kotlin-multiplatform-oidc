package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
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
        val result = webFlow.startWebFlow(request.url, request.url.parameters["redirect_uri"].orEmpty())

        if (result !is WebAuthenticationFlowResult.Success) {
            // browser closed, no redirect
            return Result.failure(OpenIdConnectException.AuthenticationCancelled())
        }

        if (result.responseUri == null) {
            return Result.failure(OpenIdConnectException.AuthenticationFailure("No Uri in callback from browser."))
        }

        return when (val error = getErrorResult<AuthCodeResult>(result.responseUri)) {
            null -> {
                val state = result.responseUri.parameters["state"]
                val code = result.responseUri.parameters["code"]
                Result.success(AuthCodeResult(code, state))
            }
            else -> {
                return error
            }
        }
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse {
        val redirectUrl = request.url.parameters["post_logout_redirect_uri"].orEmpty()
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