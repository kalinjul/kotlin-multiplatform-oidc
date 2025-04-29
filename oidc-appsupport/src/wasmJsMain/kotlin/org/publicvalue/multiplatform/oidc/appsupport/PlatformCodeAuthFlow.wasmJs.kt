package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
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
        val result = webFlow.startWebFlow(request.url)
        val code = result.parameters["code"]
        val state = result.parameters["state"]
        return Result.success(AuthCodeResult(code, state))
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse {
        webFlow.startWebFlow(request.url)
        return Result.success(Unit)
    }

    companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            WebPopupFlow.handleRedirect()
        }
    }
}

