package org.publicvalue.multiplatform.oidc.appsupport

import androidx.activity.ComponentActivity

sealed class AuthResponse {
    data class CodeResponse(val code: String?, val state: String?): AuthResponse()
    data class ErrorResponse(val error: String?): AuthResponse()
}

class HandleRedirectActivity : ComponentActivity() {

    var currentCallback: ((AuthResponse) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        val data = getIntent().getData()

        val responseUri = data
        if (responseUri?.getQueryParameterNames()?.contains("error") == true) {
            // error
            currentCallback?.invoke(AuthResponse.ErrorResponse(responseUri.getQueryParameter("error")))
        } else {
            val state = responseUri?.getQueryParameter("state")
            val code = responseUri?.getQueryParameter("code")
            currentCallback?.invoke(AuthResponse.CodeResponse(code, state))
        }
        println(data)
        finish()
    }
}