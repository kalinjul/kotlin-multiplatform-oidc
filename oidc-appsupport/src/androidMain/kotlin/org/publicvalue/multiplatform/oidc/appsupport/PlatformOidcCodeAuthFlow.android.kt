package org.publicvalue.multiplatform.oidc.appsupport

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

actual class PlatformCodeAuthFlow(
    private val context: Context,
    private val contract: ActivityResultLauncherSuspend<Intent, ActivityResult>,
    private val useWebView: Boolean = false,
    client: OpenIdConnectClient,
) : CodeAuthFlow(client) {

    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        val intent = Intent(
            context,
            HandleRedirectActivity::class.java
        )
        .apply {
            this.putExtra(EXTRA_KEY_URL, request.url.toString())
            if (useWebView) {
                this.putExtra(EXTRA_KEY_USEWEBVIEW, true)
                this.putExtra(EXTRA_KEY_REDIRECTURL, request.url.parameters.get("redirect_uri"))
            }
        }
        val result = contract.launchSuspend(intent)

        val responseUri = result.data?.data
        return if (result.resultCode == Activity.RESULT_OK && responseUri != null) {
            if (responseUri.queryParameterNames?.contains("error") == true) {
                // error
                Result.failure(OpenIdConnectException.AuthenticationFailure(message = responseUri.getQueryParameter("error") ?: ""))
            } else {
                val state = responseUri.getQueryParameter("state")
                val code = responseUri.getQueryParameter("code")
                Result.success(AuthCodeResult(code, state))
            }
        } else {
            Result.failure(OpenIdConnectException.AuthenticationFailure(message = "CustomTab result not ok (was ${result.resultCode}) or no Uri in callback from browser (was ${responseUri})."))
        }
    }
}
