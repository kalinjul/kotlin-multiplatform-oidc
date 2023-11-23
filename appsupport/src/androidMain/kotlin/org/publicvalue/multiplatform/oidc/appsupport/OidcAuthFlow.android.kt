package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

actual class PlatformOidcAuthFlow(
    private val context: Context,
    private val contract: ActivityResultLauncher<Intent>,
    client: OpenIDConnectClient,
    private val responseFlow: StateFlow<AuthResponse?>
) : OidcAuthFlow(client) {

    override suspend fun getAccessCode(request: AuthCodeRequest): AuthResponse {
        contract.launch(Intent(context, HandleRedirectActivity::class.java)
            .apply {
                this.putExtra("url", request.url.toString())
            })

        val response = responseFlow.drop(1).filterNotNull().first()
        return response
    }
}
