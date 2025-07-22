package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.customtab.getCustomTabProviders

internal const val EXTRA_KEY_USEWEBVIEW = "usewebview"
internal const val EXTRA_KEY_EPHEMERAL_SESSION = "ephemeral_session"
internal const val EXTRA_KEY_REDIRECTURL = "redirecturl"
internal const val EXTRA_KEY_URL = "url"
internal const val EXTRA_KEY_PACKAGE_NAME = "package"

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
        var createWebView: ComponentActivity.(redirectUrl: String?) -> WebView = { redirectUrl ->
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
        var showWebView: ComponentActivity.(url: String, redirectUrl: String?, epheremalSession: Boolean) -> Unit = { url, redirectUrl, epheremalSession ->
            val webView = createWebView(this, redirectUrl)
            ViewCompat.setOnApplyWindowInsetsListener(webView) { view, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = insets.top
                    leftMargin = insets.left
                    bottomMargin = insets.bottom
                    rightMargin = insets.right
                }
                WindowInsetsCompat.CONSUMED
            }
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
        val ephemeralSession = intent.extras?.getBoolean(EXTRA_KEY_EPHEMERAL_SESSION)
        val url = intent.extras?.getString(EXTRA_KEY_URL)
        val redirectUrl = intent.extras?.getString(EXTRA_KEY_REDIRECTURL)

        if (intent?.data != null) {
            // we're called by custom tab
            // create new intent for result to mitigate intent redirection vulnerability
            setResult(RESULT_OK, Intent().setData(intent?.data))
            finish()
        } else if (useWebView == true && url == null) {
            // normal resume while webview already showing, continue showing webview
        } else if (url == null) {
            // called by custom tab but no intent.data
            setResult(RESULT_CANCELED)
            finish()
        } else {
            // check if launch tab request is legit
            if (packageName == applicationContext.packageName) {
                // login requested by app
                // do not navigate to the login page again in this activity instance
                intent.removeExtra(EXTRA_KEY_URL)
                // get preferred browser if set
                val preferredBrowserPackage = intent.extras?.getString(EXTRA_KEY_PACKAGE_NAME)
                intent.removeExtra(EXTRA_KEY_PACKAGE_NAME)
                if (useWebView == true) {
                    showWebView(url, redirectUrl, ephemeralSession ?: false)
                } else {
                    launchCustomTabsIntent(
                        url,
                        redirectUrl,
                        preferredBrowserPackage,
                        ephemeralSession
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalOpenIdConnect::class)
    private fun launchCustomTabsIntent(
        url: String,
        redirectUrl: String?,
        preferredBrowserPackage: String?,
        ephemeralSession: Boolean?
    ) {
        val builder = CustomTabsIntent.Builder()
        builder.configureCustomTabsIntent()

        // Get preferred browser or first available browser for custom tabs
        val browserPackage = preferredBrowserPackage
            ?: getCustomTabProviders().firstOrNull()?.activityInfo?.packageName

        CookieManager.getInstance().removeAllCookies(null)
        if (browserPackage != null) {
            // Enable ephemeral browsing if supported
            if (CustomTabsClient.isEphemeralBrowsingSupported(this, browserPackage)) {
                builder.setEphemeralBrowsingEnabled(ephemeralSession ?: false)
            }
        } else {
            // If custom tabs are not available, fallback to WebView
            showWebView(url, redirectUrl, ephemeralSession ?: false)
            return
        }


        val intent = builder.build()

        preferredBrowserPackage?.let { intent.intent.setPackage(it) }
        intent.launchUrl(this, url.toUri())
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}