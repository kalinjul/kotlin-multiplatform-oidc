package org.publicvalue.multiplatform.oidc.appsupport.webview

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import io.ktor.http.*
import org.publicvalue.multiplatform.oidc.appsupport.*
import org.publicvalue.multiplatform.oidc.appsupport.util.toAuthenticationFlowResult

internal class WebViewFlow(
    private val context: Context,
    private val contract: ActivityResultLauncherSuspend<Intent, ActivityResult>,
    private val epheremalSession: Boolean,
): WebAuthenticationFlow {
    override suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): WebAuthenticationFlowResult {
        val intent = prepareIntent(requestUrl = requestUrl.toString(), redirectUrl = redirectUrl)
        val result = contract.launchSuspend(intent)
        return result.toAuthenticationFlowResult()
    }

    private fun prepareIntent(requestUrl: String, redirectUrl: String): Intent {
        val intent = Intent(
            context,
            HandleRedirectActivity::class.java // TODO split up activities for custom tab + web view
        )
            .apply {
                this.putExtra(EXTRA_KEY_URL, requestUrl)
                this.putExtra(EXTRA_KEY_USEWEBVIEW, true)
                this.putExtra(EXTRA_KEY_REDIRECTURL, redirectUrl)
                this.putExtra(EXTRA_KEY_EPHEMERAL_SESSION, epheremalSession)
            }
        return intent
    }
}