package org.publicvalue.multiplatform.oidc.appsupport

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

actual class PlatformCodeAuthFlow(
    context: Context,
    contract: ActivityResultLauncherSuspend<Intent, ActivityResult>,
    useWebView: Boolean = false,
    webViewEpheremalSession: Boolean = false,
    preferredBrowserPackage: String? = null,
    actual override val client: OpenIdConnectClient,
) : CodeAuthFlow, EndSessionFlow {

    private val webFlow: WebActivityFlow = WebActivityFlow(
        context = context,
        contract = contract,
        useWebView = useWebView,
        webViewEpheremalSession = webViewEpheremalSession,
        preferredBrowserPackage = preferredBrowserPackage
    )

    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())

        val responseUri = result.data?.data
        return if (result.resultCode == Activity.RESULT_OK) {
            when (val error = getErrorResult<AuthCodeResult>(responseUri)) {
                null -> {
                    val state = responseUri.getQueryParameter("state")
                    val code = responseUri.getQueryParameter("code")
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
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())

        val responseUri = result.data?.data
        return if (result.resultCode == Activity.RESULT_OK) {
            when (val error = getErrorResult<Unit>(responseUri)) {
                null -> {
                    return Result.success(Unit)
                }
                else -> {
                    return error
                }
            }
        } else {
            // browser closed, no redirect
            Result.failure(OpenIdConnectException.AuthenticationCancelled("Logout cancelled"))
        }
    }

    @OptIn(ExperimentalContracts::class)
    private fun <T> getErrorResult(responseUri: Uri?): Result<T>? {
        contract { returns(null) implies (responseUri != null) }
        if (responseUri != null) {
            if (responseUri.queryParameterNames?.contains("error") == true) {
                // error
                return Result.failure(
                    OpenIdConnectException.AuthenticationFailure(
                        message = responseUri.getQueryParameter(
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
}
