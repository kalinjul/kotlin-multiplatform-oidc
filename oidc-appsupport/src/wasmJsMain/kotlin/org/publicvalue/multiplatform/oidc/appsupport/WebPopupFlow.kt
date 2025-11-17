package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectException.TechnicalFailure
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.setResponseUri
import org.w3c.dom.MessageEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@ExperimentalOpenIdConnect
internal class WebPopupFlow(
    private val windowTarget: String = "",
    private val windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    private val redirectOrigin: String,
    private val preferences: Preferences,
): WebAuthenticationFlow {

    private class WindowHolder(var window: Window?)

    override suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): WebAuthenticationFlowResult {
        val result = suspendCoroutine<WebAuthenticationFlowResult> { continuation ->

            val popupHolder = WindowHolder(null)
            lateinit var messageHandler: (Event) -> Unit

            messageHandler = { event ->
                if (event is MessageEvent) {

                    if (event.origin != redirectOrigin) {
                        throw TechnicalFailure("Security issue. Event was not from $redirectOrigin", null)
                    }

                    if (event.source == popupHolder.window) {
                        val urlString: String = Json.decodeFromString(getEventData(event))
                        val url = Url(urlString)
                        window.removeEventListener("message", messageHandler)
                        continuation.resume(WebAuthenticationFlowResult.Success(url))
                    } else {
                        // Log an advisory but stay registered for the true callback
                        println("${WebPopupFlow::class.simpleName} skipping message from unknown source: ${event.source}")
                    }
                }
            }

            window.addEventListener("message", messageHandler)

            popupHolder.window = window.open(requestUrl.toString(), windowTarget, windowFeatures)
                ?: throw TechnicalFailure("Could not open popup", null)
        }
        if(result is WebAuthenticationFlowResult.Success) {
            // TODO refactor wasm code to just set preferences in event handler
            result.responseUri?.let {
                preferences.setResponseUri(it)
            }
        }
        return result
    }

    internal companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            if (window.opener != null) {
                postMessage(
                    url = window.location.toString(),
                    targetOrigin = getOpenerOrigin()
                )

                closeWindow(delay = 0)
            }
        }
    }
}

private fun getEventData(event: MessageEvent): String = js("JSON.stringify(event.data)")

private fun getOpenerOrigin(): String = js("window.opener.location.origin")

private fun postMessage(url: String, targetOrigin: String) {
    js("window.opener.postMessage(url, targetOrigin)")
}

private fun closeWindow(delay: Int = 100) {
    window.setTimeout(handler = { window.close(); null }, timeout = delay)
}