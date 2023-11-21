package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import androidx.activity.ComponentActivity

class HandleRedirectActivity : ComponentActivity() {

    companion object {
        var currentCallback: ((AuthResponse) -> Unit)? = null
    }

    override fun onResume() {
        super.onResume()
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
        finish()
    }
}