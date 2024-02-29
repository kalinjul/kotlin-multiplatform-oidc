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

internal const val EXTRA_KEY_USEWEBVIEW = "usewebview"
internal const val EXTRA_KEY_REDIRECTURL = "redirecturl"
internal const val EXTRA_KEY_URL = "url"

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
    }

    override fun onResume() {
        super.onResume()

        val useWebView = intent.extras?.getBoolean(EXTRA_KEY_USEWEBVIEW)
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
            if (useWebView == true) {
                showWebView(url, redirectUrl)
            } else {
                val builder = CustomTabsIntent.Builder()
                builder.configureCustomTabsIntent()
                val intent = builder.build()
                intent.launchUrl(this, Uri.parse(url))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}