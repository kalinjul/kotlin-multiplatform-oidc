package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url

internal sealed class WebAuthenticationFlowResult {
    data class Success(val responseUri: Url?) : WebAuthenticationFlowResult()
    data object Cancelled : WebAuthenticationFlowResult()
}

internal interface WebAuthenticationFlow {
    suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): WebAuthenticationFlowResult
}
