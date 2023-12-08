package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow

/**
 * Factory to create an Auth Flow on Android.
 * In order to handle redirects, the factory needs to be instantiated in your Activity's [Activity.onCreate()].
 */
@Suppress("unused")
class AndroidCodeAuthFlowFactory(
    private val activity: ComponentActivity,
    /** If true, uses an embedded WebView instead of Chrome CustomTab (not recommended) **/
    private val useWebView: Boolean = false,
): CodeAuthFlowFactory {

    lateinit var authRequestLauncher: ActivityResultLauncherSuspend<Intent, ActivityResult>

    init {
        activity.lifecycle.addObserver(
            object: LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                            authRequestLauncher = activity.registerForActivityResultSuspend(ActivityResultContracts.StartActivityForResult())
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
    }

    override fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow {
        return PlatformCodeAuthFlow(
            context = activity,
            contract = authRequestLauncher,
            client = client,
            useWebView = useWebView
        )
    }
}
