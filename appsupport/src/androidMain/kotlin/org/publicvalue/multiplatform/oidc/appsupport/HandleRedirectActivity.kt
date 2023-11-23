package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent

class HandleRedirectActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent.extras?.getString("url")
        url?.let {
            val builder = CustomTabsIntent.Builder()
            val intent = builder.build()
            intent.launchUrl(this, Uri.parse(it))
        }
    }

    var customTabStarted: Boolean = false

    override fun onResume() {
        super.onResume()

        if (intent?.data != null) {
            setResult(RESULT_OK, intent)
        }

        if (customTabStarted) {
            finish()
        }
        customTabStarted = true
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        setIntent(intent)
    }
}