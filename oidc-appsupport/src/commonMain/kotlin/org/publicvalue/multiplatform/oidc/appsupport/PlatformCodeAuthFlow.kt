package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

expect class PlatformCodeAuthFlow: CodeAuthFlow, EndSessionFlow {
    override suspend fun startLoginFlow(request: AuthCodeRequest)
    override suspend fun endSession(request: EndSessionRequest): EndSessionResponse
    override val client: OpenIdConnectClient
    override val preferences: Preferences
}

@OptIn(ExperimentalContracts::class)
internal fun <T> getErrorResult(responseUri: Url?): Result<T>? {
    contract { returns(null) implies (responseUri != null) }
    if (responseUri != null) {
        if (responseUri.parameters.contains("error")) {
            // error
            return Result.failure(
                OpenIdConnectException.AuthenticationFailure(
                    message = responseUri.parameters.get(
                        "error"
                    ) ?: ""
                )
            )
        }
    } else {
        return Result.failure(OpenIdConnectException.AuthenticationFailure(message = "No Uri in callback from browser (was ${responseUri})."))
    }
    return null
}

internal fun throwIfCancelled(result: WebAuthenticationFlowResult) {
    if (result is WebAuthenticationFlowResult.Cancelled) {
        throw OpenIdConnectException.AuthenticationCancelled()
    }
}