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

internal val EXTRA_KEY_USEWEBVIEW = "usewebview"
internal val EXTRA_KEY_REDIRECTURL = "redirecturl"
internal val EXTRA_KEY_URL = "url"

class HandleRedirectActivity : ComponentActivity() {

    companion object {
        /** Set to use your own web settings when using WebView **/
        var configureWebSettings: WebSettings.() -> Unit = {
            javaScriptCanOpenWindowsAutomatically = false
            setSupportMultipleWindows(false)
        }

        /** Set to implement your own WebView. **/
        var showWebView: Activity.(url: String, redirectUrl: String?) -> Unit = { url, redirectUrl ->
            val webView = WebView(this)
            setContentView(webView)

            CookieManager.getInstance().removeAllCookies({})
            webView.clearHistory()
            webView.clearCache(true)
            webView.settings.apply {
                configureWebSettings()
            }
            webView.webChromeClient = WebChromeClient()
            webView.webViewClient = object : WebViewClient() {

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
            webView.loadUrl(url)
        }

        /** Set to customize the custom tabs intent **/
        var configureCustomTabsIntent: CustomTabsIntent.Builder.() -> Unit = {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val useWebView = intent.extras?.getBoolean(EXTRA_KEY_USEWEBVIEW)
        val url = intent.extras?.getString(EXTRA_KEY_URL)
        val redirectUrl = intent.extras?.getString(EXTRA_KEY_REDIRECTURL)

        if (url == null) { // we're coming back from custom tab without an url set
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        if (useWebView == true) {
            showWebView(url, redirectUrl)
        } else {
            val builder = CustomTabsIntent.Builder()
            builder.configureCustomTabsIntent()
            val intent = builder.build()
            intent.launchUrl(this, Uri.parse(url))
        }
    }

    private var customTabStarted: Boolean = false

    override fun onResume() {
        super.onResume()

        if (intent?.data != null) {
            setResult(RESULT_OK, intent)
        }

        if (customTabStarted) {
            finish()
        }
        customTabStarted = true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setIntent(intent)
    }
}