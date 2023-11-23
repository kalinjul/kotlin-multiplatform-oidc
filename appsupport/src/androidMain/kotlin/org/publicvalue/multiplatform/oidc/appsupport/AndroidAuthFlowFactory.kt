package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

class AndroidAuthFlowFactory(
    val activity: ComponentActivity
): AuthFlowFactory {

    lateinit var authRequestContract: ActivityResultLauncherSuspend<Intent>

    init {
        activity.lifecycle.addObserver(
            object: LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                            authRequestContract = activity.registerForActivityResultSuspend(ActivityResultContracts.StartActivityForResult())
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

    override fun createAuthFlow(client: OpenIDConnectClient): OidcAuthFlow {
        return PlatformOidcAuthFlow(
            context = activity,
            contract = authRequestContract,
            client = client,
        )
    }
}
