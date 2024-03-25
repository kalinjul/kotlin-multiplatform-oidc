package org.publicvalue.multiplatform.oidc.appsupport

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

internal const val EXTRA_KEY_USEWEBVIEW = "usewebview"
internal const val EXTRA_KEY_WEBVIEW_EPHEREMAL_SESSION = "webview_epheremal_session"
internal const val EXTRA_KEY_REDIRECTURL = "redirecturl"
internal const val EXTRA_KEY_URL = "url"

class HandleRedirectActivity : ComponentActivity() {

    companion object {
        /** Set to use your own web settings when using WebView **/
        @Deprecated(message = "Set configureWebView instead and use webView.settings.apply()")
        var configureWebSettings: WebSettings.() -> Unit = { }

        @Suppress("DEPRECATION")
        private val defaultConfigureWebView: (WebView) -> Unit = { webView ->
            webView.settings.apply {
                javaScriptCanOpenWindowsAutomatically = false
                setSupportMultipleWindows(false)
                configureWebSettings()
            }
        }

        /** Set to use custom configuration when using WebView **/
        @ExperimentalOpenIdConnect
        var configureWebView: (WebView) -> Unit = defaultConfigureWebView


        @ExperimentalOpenIdConnect
        var createWebView: Activity.(redirectUrl: String?) -> WebView = { redirectUrl ->
            WebView(this).apply {
                configureWebView(this)
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestedUrl = request?.url
                        return if (requestedUrl != null && redirectUrl != null && requestedUrl.toString().startsWith(redirectUrl)) {
                            intent.data = request.url
                            setResult(RESULT_OK, intent)
                            finish()
                            true
                        } else {
                            false
                        }
                    }
                }
            }
        }

        @ExperimentalOpenIdConnect
        var showWebView: Activity.(url: String, redirectUrl: String?, epheremalSession: Boolean) -> Unit = { url, redirectUrl, epheremalSession ->
            val webView = createWebView(this, redirectUrl)
            if (epheremalSession) {
                CookieManager.getInstance().removeAllCookies(null)
                webView.clearHistory()
                webView.clearCache(true)
            }
            setContentView(webView)
            webView.loadUrl(url)
        }

        /** Set to customize the custom tabs intent **/
        var configureCustomTabsIntent: CustomTabsIntent.Builder.() -> Unit = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            // this activity already navigated to login page, which was probably closed by the user
            // do not navigate to the login page again
            intent.removeExtra(EXTRA_KEY_URL)
        }
    }

    @OptIn(ExperimentalOpenIdConnect::class)
    override fun onResume() {
        super.onResume()

        val useWebView = intent.extras?.getBoolean(EXTRA_KEY_USEWEBVIEW)
        val webViewEpheremalSession = intent.extras?.getBoolean(EXTRA_KEY_WEBVIEW_EPHEREMAL_SESSION)
        val url = intent.extras?.getString(EXTRA_KEY_URL)
        val redirectUrl = intent.extras?.getString(EXTRA_KEY_REDIRECTURL)

        if (intent?.data != null) {
            // we're called by custom tab
            setResult(RESULT_OK, intent)
            finish()
        } else if (url == null) {
            // called by custom tab but no intent.data
            setResult(RESULT_CANCELED)
            finish()
        } else {
            // login requested by app
            // do not navigate to the login page again in this activity instance
            intent.removeExtra(EXTRA_KEY_URL)
            if (useWebView == true) {
                showWebView(url, redirectUrl, webViewEpheremalSession ?: false)
            } else {
                launchCustomTabsIntent(url)
            }
        }
    }

    private fun launchCustomTabsIntent(url: String?) {
        val builder = CustomTabsIntent.Builder()
        builder.configureCustomTabsIntent()
        val intent = builder.build()
        intent.launchUrl(this, Uri.parse(url))
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}