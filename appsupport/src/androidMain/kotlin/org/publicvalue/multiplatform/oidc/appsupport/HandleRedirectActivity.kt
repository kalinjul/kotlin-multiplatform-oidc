package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Intent
import androidx.activity.ComponentActivity
import kotlin.reflect.KClass

class HandleRedirectActivity : ComponentActivity() {

    companion object {
        internal var currentCallback: ((AuthResponse) -> Unit)? = null

        /**
         * If set, force redirect to the given Activity after redirect.
         * Set to your MainActivity if the Login Custom Tab does not close automatically.
         */
        var mainActivityClass: KClass<*>? = null
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

        mainActivityClass?.let {
            val intent = Intent(this, it.java);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent)
        }

        finish()
    }
}