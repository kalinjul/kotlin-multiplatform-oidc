package org.publicvalue.multiplatform.oidc.appsupport.customtab

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.appsupport.ActivityResultLauncherSuspend
import org.publicvalue.multiplatform.oidc.appsupport.EXTRA_KEY_EPHEMERAL_SESSION
import org.publicvalue.multiplatform.oidc.appsupport.EXTRA_KEY_PACKAGE_NAME
import org.publicvalue.multiplatform.oidc.appsupport.EXTRA_KEY_REDIRECTURL
import org.publicvalue.multiplatform.oidc.appsupport.EXTRA_KEY_URL
import org.publicvalue.multiplatform.oidc.appsupport.HandleRedirectActivity
import org.publicvalue.multiplatform.oidc.appsupport.WebAuthenticationFlow
import org.publicvalue.multiplatform.oidc.appsupport.WebAuthenticationFlowResult
import org.publicvalue.multiplatform.oidc.appsupport.util.toAuthenticationFlowResult

internal class CustomTabFlow(
    private val context: Context,
    private val contract: ActivityResultLauncherSuspend<Intent, ActivityResult>,
    private val epheremalSession: Boolean,
    private val preferredBrowserPackage: String?,
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
                this.putExtra(EXTRA_KEY_PACKAGE_NAME, preferredBrowserPackage)
                this.putExtra(EXTRA_KEY_REDIRECTURL, redirectUrl) // if fallback to webview is triggered
                this.putExtra(EXTRA_KEY_EPHEMERAL_SESSION, epheremalSession)
            }
        return intent
    }
}