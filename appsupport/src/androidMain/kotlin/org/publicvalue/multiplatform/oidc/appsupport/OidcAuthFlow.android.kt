package org.publicvalue.multiplatform.oidc.appsupport

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

actual class PlatformOidcAuthFlow(
    private val context: Context,
    private val contract: ActivityResultLauncherSuspend<Intent>,
    client: OpenIDConnectClient,
) : OidcAuthFlow(client) {

    override suspend fun getAccessCode(request: AuthCodeRequest): AuthResponse {
        val intent = Intent(context, HandleRedirectActivity::class.java).apply {
            this.putExtra("url", request.url.toString())
            this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val result = contract.launchSuspend(intent)

        val responseUri = result.data?.data
        return if (result.resultCode == Activity.RESULT_OK && responseUri != null) {
            if (responseUri.queryParameterNames?.contains("error") == true) {
                // error
                AuthResponse.ErrorResponse(responseUri.getQueryParameter("error"))
            } else {
                val state = responseUri.getQueryParameter("state")
                val code = responseUri.getQueryParameter("code")
                AuthResponse.CodeResponse(code, state)
            }
        } else {
            AuthResponse.ErrorResponse("No Uri in callback from browser.")
        }
    }
}
