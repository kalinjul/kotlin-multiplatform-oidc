package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import androidx.activity.ComponentActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class HandleRedirectActivity : ComponentActivity() {

    companion object {
        var currentCallback: ((AuthResponse) -> Unit)? = null
    }

    override fun onResume() {
        super.onResume()
        println("HandleRedirectActivity.OnResume")
        val data = getIntent().getData()

        val responseUri = data
        if (responseUri?.queryParameterNames?.contains("error") == true) {
            // error
            currentCallback?.invoke(AuthResponse.ErrorResponse(responseUri.getQueryParameter("error")))
        } else {
            val state = responseUri?.getQueryParameter("state")
            val code = responseUri?.getQueryParameter("code")
            currentCallback?.invoke(AuthResponse.CodeResponse(code, state))
        }
        println("Calling finish()")

        finish()
    }
}