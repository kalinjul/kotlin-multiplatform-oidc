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

        val url = intent.extras?.getString(EXTRA_KEY_URL)
        println("###### Activity created $this")
        println("######  with url $url")
        println("######  and data ${intent.data}")

    }

    override fun onResume() {
        super.onResume()

        val useWebView = intent.extras?.getBoolean(EXTRA_KEY_USEWEBVIEW)
        val url = intent.extras?.getString(EXTRA_KEY_URL)
        val redirectUrl = intent.extras?.getString(EXTRA_KEY_REDIRECTURL)

        println("###### Activity resumed $this")
        println("######  with url $url")
        println("######  and data ${intent.data}")


        if (intent?.data != null) {
            println("###### Activity reporting RESULT_OK $this")
            setResult(RESULT_OK, intent)
            finish()

        } else if (url == null) { // we're started without a url
            println("###### Activity reporting CANCELED $this")
            setResult(RESULT_CANCELED)
            finish()

        } else {
            if (useWebView == true) {
                println("###### Activity showing webview $this")
                showWebView(url, redirectUrl)
            } else {
                println("###### Activity launchnig CustomTabs $this")
                val builder = CustomTabsIntent.Builder()
                builder.configureCustomTabsIntent()
                val intent = builder.build()
                intent.launchUrl(this, Uri.parse(url))
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        println("###### Activity newIntent $this")
        println("######  with data ${intent?.data}")
        setIntent(intent)
    }

    override fun onDestroy() {
        println("###### Activity destroyed $this")
        super.onDestroy()
    }
}