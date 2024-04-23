package org.publicvalue.multiplatform.oidc.appsupport

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthRequest
import org.publicvalue.multiplatform.oidc.types.remote.AuthResponse
import org.publicvalue.multiplatform.oidc.types.remote.AuthResult

actual class PlatformAuthFlow(
    private val context: Context,
    private val contract: ActivityResultLauncherSuspend<Intent, ActivityResult>,
    private val useWebView: Boolean = false,
    private val webViewEpheremalSession: Boolean = false,
    client: OpenIdConnectClient,
) : AuthFlow(client) {

    override suspend fun getAuthorizationResult(request: AuthRequest): AuthResponse {
        val intent = Intent(
            context,
            HandleRedirectActivity::class.java
        ).apply {
            this.putExtra(EXTRA_KEY_URL, request.url.toString())
            if (useWebView) {
                this.putExtra(EXTRA_KEY_USEWEBVIEW, true)
                request.url.parameters.get("redirect_uri")?.let {
                    this.putExtra(EXTRA_KEY_REDIRECTURL, it)
                }
                this.putExtra(EXTRA_KEY_WEBVIEW_EPHEREMAL_SESSION, webViewEpheremalSession)
            }
        }
        val result = contract.launchSuspend(intent)

        val responseUri = result.data?.data
        return if (result.resultCode == Activity.RESULT_OK && responseUri != null) {
            if (responseUri.queryParameterNames?.contains("error") == true) {
                Result.failure(OpenIdConnectException.AuthenticationFailure(message = responseUri.getQueryParameter("error") ?: ""))
            } else {
                val state = responseUri.getQueryParameter("state")?.ifBlank { null }
                val code = responseUri.getQueryParameter("code")?.ifBlank { null }
                if (code == null) {
                    val accessToken = responseUri.getQueryParameter("access_token")?.ifBlank { null }
                    if (accessToken != null) {
                        return AuthResponse.success(
                            AuthResult.AccessToken(
                                access_token = accessToken,
                                token_type = responseUri.getQueryParameter("token_type")?.ifBlank { null },
                                expires_in = responseUri.getQueryParameter("expires_in")?.ifBlank { null }?.toIntOrNull()
                            )
                        )
                    }
                }
                AuthResponse.success(AuthResult.Code(code, state))
            }
        } else {
            Result.failure(OpenIdConnectException.AuthenticationFailure(message = "CustomTab result not ok (was ${result.resultCode}) or no Uri in callback from browser (was ${responseUri})."))
        }
    }
}
