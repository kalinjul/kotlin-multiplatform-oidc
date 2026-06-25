package org.publicvalue.multiplatform.oidc.appsupport

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore
import org.publicvalue.multiplatform.oidc.preferences.setResponseUri

internal const val EXTRA_KEY_USEWEBVIEW = "usewebview"
internal const val EXTRA_KEY_EPHEMERAL_SESSION = "ephemeral_session"
internal const val EXTRA_KEY_REDIRECTURL = "redirecturl"
internal const val EXTRA_KEY_URL = "url"
internal const val EXTRA_KEY_PACKAGE_NAME = "package"
internal const val EXTRA_KEY_UNSUPPORTED_EPHEMERAL_CUSTOM_TABS_FALLBACK = "unsupported_ephemeral_custom_tabs_fallback"

class HandleRedirectActivity : ComponentActivity() {

    private var clearWebViewDataOnDestroy = false
    private var currentWebView: WebView? = null

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
            val context = this
            WebView(context).apply {
                configureWebView(this)
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestedUrl = request?.url
                        return if (requestedUrl != null && redirectUrl != null && requestedUrl.toString().startsWith(redirectUrl)) {
                            val preferences = PreferencesDataStore(context.dataStore)
                            runBlocking {
                                preferences.setResponseUri(Url(requestedUrl.toString()))
                            }
                            setResult(RESULT_OK, Intent().setData(intent?.data))
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
        var showWebView: ComponentActivity.(url: String, redirectUrl: String?, ephemeralSession: Boolean) -> Unit = { url, redirectUrl, ephemeralSession ->
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
            if (ephemeralSession) {
                (this as? HandleRedirectActivity)?.apply {
                    clearWebViewSessionData(webView)
                    clearWebViewDataOnDestroy = true
                    currentWebView = webView
                }
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
        val unsupportedEphemeralCustomTabsFallback = intent.extras
            ?.getString(EXTRA_KEY_UNSUPPORTED_EPHEMERAL_CUSTOM_TABS_FALLBACK)
            ?.toUnsupportedEphemeralCustomTabsFallback()
            ?: UnsupportedEphemeralCustomTabsFallback.PrivateWebView

        if (intent?.data != null) {
            // we're called by custom tab
            runBlocking {
                val preferences = PreferencesDataStore(this@HandleRedirectActivity.dataStore)
                preferences.setResponseUri(Url(intent?.data.toString()))
            }
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
                    showWebViewSession(url, redirectUrl, ephemeralSession ?: false)
                } else {
                    launchCustomTabsIntent(
                        url,
                        redirectUrl,
                        preferredBrowserPackage,
                        ephemeralSession,
                        unsupportedEphemeralCustomTabsFallback
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
        ephemeralSession: Boolean?,
        unsupportedEphemeralCustomTabsFallback: UnsupportedEphemeralCustomTabsFallback
    ) {
        val builder = CustomTabsIntent.Builder()
        builder.configureCustomTabsIntent()

        val ephemeralSessionRequested = ephemeralSession ?: false
        if (preferredBrowserPackage != null) {
            val launchMode = resolveEphemeralCustomTabsLaunchMode(
                ephemeralSession = ephemeralSessionRequested,
                ephemeralBrowsingSupported = CustomTabsClient.isEphemeralBrowsingSupported(this, preferredBrowserPackage),
                unsupportedEphemeralCustomTabsFallback = unsupportedEphemeralCustomTabsFallback
            )

            when (launchMode) {
                EphemeralCustomTabsLaunchMode.EphemeralCustomTab -> builder.setEphemeralBrowsingEnabled(true)
                EphemeralCustomTabsLaunchMode.PrivateWebView -> {
                    showWebViewSession(url, redirectUrl, true)
                    return
                }
                EphemeralCustomTabsLaunchMode.NormalCustomTab -> Unit
            }
        }

        val intent = builder.build()

        preferredBrowserPackage.let { intent.intent.setPackage(it) }
        try {
            intent.launchUrl(this, url.toUri())
        } catch (_: ActivityNotFoundException) {
            // If there is no browser activity available, fallback to WebView
            showWebViewSession(url, redirectUrl, ephemeralSessionRequested)
        }
    }

    @OptIn(ExperimentalOpenIdConnect::class)
    private fun showWebViewSession(url: String, redirectUrl: String?, ephemeralSession: Boolean) {
        intent.putExtra(EXTRA_KEY_USEWEBVIEW, true)
        showWebView(url, redirectUrl, ephemeralSession)
    }

    private fun clearWebViewSessionData(webView: WebView?) {
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        WebStorage.getInstance().deleteAllData()
        webView?.clearHistory()
        webView?.clearCache(true)
    }

    override fun onDestroy() {
        if (clearWebViewDataOnDestroy) {
            clearWebViewSessionData(currentWebView)
            currentWebView = null
            clearWebViewDataOnDestroy = false
        }
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

private fun String.toUnsupportedEphemeralCustomTabsFallback(): UnsupportedEphemeralCustomTabsFallback? =
    UnsupportedEphemeralCustomTabsFallback.entries.firstOrNull { it.name == this }
