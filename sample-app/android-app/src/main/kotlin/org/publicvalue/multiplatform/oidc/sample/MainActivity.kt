package org.publicvalue.multiplatform.oidc.sample

import MainView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory


class MainActivity : ComponentActivity() {

    // There should only be one instance of this factory.
    // The flow should also be created and started from an
    // Application or ViewModel scope, so it persists Activity.onDestroy() e.g. on low memory
    // and is still able to process redirect results during login.
    val codeAuthFlowFactory = AndroidCodeAuthFlowFactory(useWebView = false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeAuthFlowFactory.registerActivity(this)
        setContent {
            MainView(
                codeAuthFlowFactory
            )
        }
    }
}