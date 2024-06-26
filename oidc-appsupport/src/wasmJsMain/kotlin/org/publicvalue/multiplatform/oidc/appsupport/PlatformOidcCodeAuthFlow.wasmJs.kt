package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.coroutines.delay
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.w3c.dom.Window


actual class PlatformCodeAuthFlow(
    client: OpenIdConnectClient
) : CodeAuthFlow(client) {


    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        //window.location.replace(request.url.toString())
        val loginWindow = window.open(request.url.toString())

        // WAIT FOR EXTERNAL INPUT
        val resultParameters = waitForRedirectUri(loginWindow)
        return AuthCodeResponse.success(
            AuthCodeResult(
                code = resultParameters!!["code"],
                state = resultParameters["state"]
            )
        )

    }

    suspend fun waitForRedirectUri(loginWindow: Window?): Parameters? {
        while (true) {
            var parameters: Parameters? = null
            jsCatch {
                try {
                    val loginWindowUrl = Url(loginWindow?.location.toString())
                    if (loginWindowUrl.parameters.contains("code") && loginWindowUrl.parameters.contains("state")) {
                        println("Ok received all")
                        loginWindow?.close()
                        parameters = loginWindowUrl.parameters
                    } else {
                        println("Waiting")
                    }
                } catch (e: Exception) {
                    println("I'm running, but login not completed")
                }
            }

            if (parameters != null) {
                return parameters
            }

            delay(100)
        }
    }

}

fun jsCatch(f: () -> Unit): JsAny? {
    js("""
    let result = null;
    try { 
        f();
    } catch (e) {
       result = e;
    }
    return result;
    """)
}