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
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow

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
    /** If true, uses an embedded WebView instead of Chrome CustomTab (not recommended) **/
    private val useWebView: Boolean = false,
): CodeAuthFlowFactory {

    lateinit var authRequestLauncher: ActivityResultLauncherSuspend<Intent, ActivityResult>
    lateinit var context: Context

    private val resultFlow: MutableStateFlow<ActivityResult?> = MutableStateFlow(null)

    @Deprecated(
        message = "Use AndroidCodeAuthFlowFactory(useWebView: Boolean) instead and call registerActivity().",
        replaceWith = ReplaceWith("AndroidCodeAuthFlowFactory(useWebView).also { it.registerActivity(activity) }"))
    constructor(activity: ComponentActivity, useWebView: Boolean) : this(useWebView = useWebView) {
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
                            authRequestLauncher = activity.registerForActivityResultSuspend(resultFlow, ActivityResultContracts.StartActivityForResult())
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
    }

    override fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow {
        return PlatformCodeAuthFlow(
            context = context,
            contract = authRequestLauncher,
            client = client,
            useWebView = useWebView
        )
    }
}
