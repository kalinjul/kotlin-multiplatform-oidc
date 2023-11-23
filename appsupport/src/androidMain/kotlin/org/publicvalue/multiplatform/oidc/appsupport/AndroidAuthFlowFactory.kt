package org.publicvalue.multiplatform.oidc.appsupport

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

class AndroidAuthFlowFactory(
    val activity: ComponentActivity
): AuthFlowFactory {

    lateinit var authRequestContract: ActivityResultLauncher<Intent>

    var authResponse: MutableStateFlow<AuthResponse?> = MutableStateFlow(null)

    init {
        activity.lifecycle.addObserver(
            object: LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> {
                            authRequestContract = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                                val responseUri = it.data?.data
                                if (it.resultCode == Activity.RESULT_OK && responseUri != null) {
                                    if (responseUri.queryParameterNames?.contains("error") == true) {
                                        // error
                                        authResponse.value = AuthResponse.ErrorResponse(responseUri.getQueryParameter("error"))
                                    } else {
                                        val state = responseUri.getQueryParameter("state")
                                        val code = responseUri.getQueryParameter("code")
                                        authResponse.value = AuthResponse.CodeResponse(code, state)
                                    }
                                } else {
                                    authResponse.value = AuthResponse.ErrorResponse("No Uri in callback from browser.")
                                }
                            }
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
            responseFlow = authResponse.asStateFlow()
        )
    }
}
