package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectException.TechnicalFailure
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
) {
    internal suspend fun startWebFlow(requestUrl: Url): Url {
        return suspendCoroutine<Url> { continuation ->

            lateinit var popup: Window
            lateinit var messageHandler: (Event) -> Unit

            messageHandler = { event ->
                if (event is MessageEvent) {

                    if (event.origin != redirectOrigin)
                        throw TechnicalFailure("Security issue. Event was not from ${window.location.origin}", null)

                    if (event.source == popup) {
                        val urlString: String = Json.decodeFromString(getEventData(event))
                        val url = Url(urlString)
                        window.removeEventListener("message", messageHandler)
                        continuation.resume(url)
                    } else {
                        // Log an advisory but stay registered for the true callback
                        println("${WebPopupFlow::class.simpleName} skipping message from unknown source: ${event.source}")
                    }
                }
            }

            window.addEventListener("message", messageHandler)

            popup = window.open(requestUrl.toString(), windowTarget, windowFeatures)
                ?: throw TechnicalFailure("Could not open popup", null)
        }
    }

    internal companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            val opener = window.opener as Window?
            if (opener != null) {
                opener.postMessage(
                    message = window.location.toString().toJsString(),
                    targetOrigin = opener.location.origin
                )

                window.setTimeout(
                    handler = {
                        window.close()
                        null
                    },
                    timeout = 0
                )
            } else {
                println("No window opener, not a popup flow.")
            }
        }
    }
}

private fun getEventData(event: MessageEvent): String = js("JSON.stringify(event.data)")
