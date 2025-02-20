package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException.TechnicalFailure
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.MessageEvent
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalOpenIdConnect
actual class PlatformCodeAuthFlow(
    client: OpenIdConnectClient,
    val windowTarget: String = "",
    val windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    val redirectOrigin: String
) : CodeAuthFlow(client) {

    @ExperimentalOpenIdConnect
    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse = suspendCoroutine<AuthCodeResponse> { continuation ->
        var popup = window.open(request.url.toString(), windowTarget, windowFeatures)

        if (popup == null) {
            continuation.resume(AuthCodeResponse.failure(TechnicalFailure("No popup opened", null)))
        }

        val messageHandler: (Event) -> Unit = { event ->
            if (event is MessageEvent) {

                if (event.origin != redirectOrigin)
                    continuation.resume(AuthCodeResponse.failure(TechnicalFailure("Security issue. Event was not from ${window.location.origin}", null)))

                if (event.source == popup) {
                    val authCodeResult: AuthCodeResult = Json.decodeFromString(getEventData(event))

                    continuation.resume(AuthCodeResponse.success(authCodeResult))
                }
            }
        }

        window.addEventListener("message", messageHandler, AddEventListenerOptions(
            once = true
        ))
    }

    companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            if (window.opener != null) {
                postMessage(
                    code = Url(window.location.toString()).parameters["code"],
                    state = Url(window.location.toString()).parameters["state"],
                    targetOrigin = getOpenerOrigin()
                )

                closeTheWindow(delay = 0)
            }
        }
    }
}

private fun getEventData(event: MessageEvent): String = js("JSON.stringify(event.data)")

private fun getOpenerOrigin(): String = js("window.opener.location.origin")

private fun postMessage(code: String?, state: String?, targetOrigin: String) {
    js("window.opener.postMessage({ code: code, state: state}, targetOrigin)")
}

private fun closeTheWindow(delay: Int = 100) {
    js("setTimeout(() => window.close(), delay)")
}
