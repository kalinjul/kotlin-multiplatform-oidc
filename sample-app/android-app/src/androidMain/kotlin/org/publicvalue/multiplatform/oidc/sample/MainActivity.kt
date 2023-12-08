package org.publicvalue.multiplatform.oidc.sample

import MainView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = AndroidCodeAuthFlowFactory(this, useWebView = false)
        setContent {
            MainView(
                factory
            )
        }
    }
}