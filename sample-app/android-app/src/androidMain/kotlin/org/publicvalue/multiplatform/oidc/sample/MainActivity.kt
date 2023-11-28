package org.publicvalue.multiplatform.oidc.sample

import MainView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.publicvalue.multiplatform.oidc.appsupport.AndroidAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.HandleRedirectActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = AndroidAuthFlowFactory(this)
        setContent {
            MainView(
                factory
            )
        }
    }
}