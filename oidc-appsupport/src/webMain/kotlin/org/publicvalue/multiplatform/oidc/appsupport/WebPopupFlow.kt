package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@ExperimentalOpenIdConnect
internal expect class WebPopupFlow(
    windowTarget: String = "",
    windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    redirectOrigin: String,
) : WebAuthenticationFlow {
    override suspend fun startWebFlow(
        requestUrl: Url,
        redirectUrl: String
    ): WebAuthenticationFlowResult

    companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect()
    }
}