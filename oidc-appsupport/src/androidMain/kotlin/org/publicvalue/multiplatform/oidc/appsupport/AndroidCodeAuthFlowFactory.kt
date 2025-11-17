package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.customtab.CustomTabFlow
import org.publicvalue.multiplatform.oidc.appsupport.customtab.getCustomTabProviders
import org.publicvalue.multiplatform.oidc.appsupport.webview.WebViewFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore

/**
 * Factory to create an Auth Flow on Android.
 * There should only be a single instance of this factory.
 *
 * In order to handle redirects, the factory needs to be instantiated in your Activity's
 * [Activity.onCreate()] using a non-null activity parameter or you must call registerActivity()
 * inside your Activity's [Activity.onCreate()].
 */
@Suppress("unused")
class AndroidCodeAuthFlowFactory(
    /**
     * If `true`, an embedded WebView is used for the authorization flow.
     * This is generally **not recommended** due to security and UX concerns.
     *
     * If `false` (default), Chrome Custom Tabs are preferred (if available),
     * falling back to a WebView only if no suitable browser is found.
     */
    private val useWebView: Boolean = false,

    /**
     * If `true`, the authorization session will be ephemeral:
     * cookies, cache, and other session data will be cleared before starting
     * the flow in both WebView and Custom Tabs (if supported).
     */
    private val ephemeralSession: Boolean = false,
    /** preferred custom tab providers, list of package names in order of priority. Check [Browser][org.publicvalue.multiplatform.oidc.appsupport.customtab.Browser] for example values. **/
    private val customTabProviderPriority: List<String> = listOf(),
): CodeAuthFlowFactory {

    private lateinit var activityResultLauncher: ActivityResultLauncherSuspend<Intent, ActivityResult>
    private lateinit var context: Context
    private lateinit var preferences: Preferences

    private val resultFlow: MutableStateFlow<ActivityResult?> = MutableStateFlow(null)

    @Deprecated(
        message = "Use AndroidCodeAuthFlowFactory(useWebView: Boolean) instead and call registerActivity().",
        replaceWith = ReplaceWith("AndroidCodeAuthFlowFactory(useWebView).also { it.registerActivity(activity) }"))
    constructor(activity: ComponentActivity, useWebView: Boolean = false) : this(useWebView = useWebView) {
        registerActivity(activity)
    }

    /**
     * Registers a lifecycle observer to be able to start a browser when required for login.
     */
    fun registerActivity(activity: ComponentActivity) {
        activity.lifecycle.addObserver(
            object: LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                            activityResultLauncher = activity.registerForActivityResultSuspend(resultFlow, ActivityResultContracts.StartActivityForResult())
                        }

                        Lifecycle.Event.ON_DESTROY -> {
                            activity.lifecycle.removeObserver(this)
                        }

                        else -> {

                        }
                    }
                }
            }
        )
        this.context = activity.applicationContext
        this.preferences = PreferencesDataStore(context.dataStore)
    }

    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        val customTabProviders = context.getCustomTabProviders().map { it.activityInfo.packageName }
        val preferredBrowserPackage = if (customTabProviderPriority.isNotEmpty()) {
            val presentPreferredProviders =
                customTabProviderPriority.filter { customTabProviders.contains(it) }
            presentPreferredProviders.firstOrNull()
        } else customTabProviders.firstOrNull()

        val webFlow = if (useWebView) {
            WebViewFlow(
                context = context,
                contract = activityResultLauncher,
                epheremalSession = ephemeralSession
            )
        } else {
            CustomTabFlow(
                context = context,
                contract = activityResultLauncher,
                epheremalSession = ephemeralSession,
                preferredBrowserPackage = preferredBrowserPackage,
            )
        }
        return PlatformCodeAuthFlow(
            client = client,
            webFlow = webFlow,
            preferences = preferences
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}
