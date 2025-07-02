package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResult
import io.ktor.http.Url

internal class WebActivityFlow(
    private val context: Context,
    private val contract: ActivityResultLauncherSuspend<Intent, ActivityResult>,
    private val useWebView: Boolean,
    private val webViewEpheremalSession: Boolean,
    private val preferredBrowserPackage: String?,
) {
    internal suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): ActivityResult {
        val intent = prepareIntent(requestUrl = requestUrl.toString(), redirectUrl = redirectUrl)
        val result = contract.launchSuspend(intent)
        return result
    }

    private fun prepareIntent(requestUrl: String, redirectUrl: String): Intent {
        val intent = Intent(
            context,
            HandleRedirectActivity::class.java
        )
            .apply {
                this.putExtra(EXTRA_KEY_URL, requestUrl)
                this.putExtra(EXTRA_KEY_PACKAGE_NAME, preferredBrowserPackage)
                if (useWebView) {
                    this.putExtra(EXTRA_KEY_USEWEBVIEW, true)
                    this.putExtra(EXTRA_KEY_REDIRECTURL, redirectUrl)
                    this.putExtra(EXTRA_KEY_WEBVIEW_EPHEREMAL_SESSION, webViewEpheremalSession)
                }
            }
        return intent
    }
}