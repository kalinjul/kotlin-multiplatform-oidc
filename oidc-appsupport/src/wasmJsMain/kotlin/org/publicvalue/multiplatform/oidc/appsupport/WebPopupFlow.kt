package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectException.TechnicalFailure
import org.w3c.dom.AddEventListenerOptions
import org.w3c.dom.MessageEvent
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
            val popup = window.open(requestUrl.toString(), windowTarget, windowFeatures)

            if (popup == null) {
                throw TechnicalFailure("No popup opened", null)
            }

            val messageHandler: (Event) -> Unit = { event ->
                if (event is MessageEvent) {

                    if (event.origin != redirectOrigin)
                        throw TechnicalFailure("Security issue. Event was not from ${window.location.origin}", null)

                    if (event.source == popup) {
                        val urlString: String = Json.decodeFromString(getEventData(event))
                        val url = Url(urlString)
                        continuation.resume(url)
                    }
                }
            }

            window.addEventListener("message", messageHandler, AddEventListenerOptions(
                    once = true
                )
            )
        }
    }

    internal companion object {
        @ExperimentalOpenIdConnect
        fun handleRedirect() {
            if (window.opener != null) {
                postMessage(
                    url = window.location.toString(),
                    targetOrigin = getOpenerOrigin()
                )

                closeTheWindow(delay = 0)
            }
        }
    }
}

private fun getEventData(event: MessageEvent): String = js("JSON.stringify(event.data)")

private fun getOpenerOrigin(): String = js("window.opener.location.origin")

private fun postMessage(url: String, targetOrigin: String) {
    js("window.opener.postMessage(url, targetOrigin)")
}

private fun closeTheWindow(delay: Int = 100) {
    js("setTimeout(() => window.close(), delay)")
}