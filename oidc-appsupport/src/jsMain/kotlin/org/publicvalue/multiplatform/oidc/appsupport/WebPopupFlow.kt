package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.browser.window
import kotlinx.coroutines.suspendCancellableCoroutine
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.w3c.dom.MessageEvent
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import kotlin.coroutines.resume

@ExperimentalOpenIdConnect
internal actual class WebPopupFlow actual constructor(
    private val windowTarget: String,
    private val windowFeatures: String,
    private val redirectOrigin: String,
) : WebAuthenticationFlow {

    actual override suspend fun startWebFlow(
        requestUrl: Url,
        redirectUrl: String
    ): WebAuthenticationFlowResult = suspendCancellableCoroutine { continuation ->
        val popup: Window? = window.open(requestUrl.toString(), windowTarget, windowFeatures)
        if (popup == null) {
            continuation.resume(WebAuthenticationFlowResult.Cancelled)
            return@suspendCancellableCoroutine
        }

        var intervalId: Int? = null
        lateinit var messageListener: (Event) -> Unit

        messageListener = messageListener@{ event: Event ->
            val messageEvent = event as? MessageEvent ?: return@messageListener
            if (messageEvent.origin != redirectOrigin) {
                return@messageListener
            }
            if (messageEvent.source != popup) {
                return@messageListener
            }
            val urlString = when (val data = messageEvent.data) {
                is String -> data
                else -> JSON.stringify(data)
            }
            window.removeEventListener("message", messageListener)
            intervalId?.let { window.clearInterval(it) }
            continuation.resume(WebAuthenticationFlowResult.Success(Url(urlString)))
            popup.close()
        }

        window.addEventListener("message", messageListener)

        intervalId = window.setInterval({
            if (popup.closed) {
                window.removeEventListener("message", messageListener)
                intervalId?.let { window.clearInterval(it) }
                if (continuation.isActive) {
                    continuation.resume(WebAuthenticationFlowResult.Cancelled)
                }
            }
        }, 500)

        continuation.invokeOnCancellation {
            window.removeEventListener("message", messageListener)
            intervalId.let { window.clearInterval(it) }
            if (!popup.closed) {
                popup.close()
            }
        }
    }

    actual companion object {
        @ExperimentalOpenIdConnect
        actual fun handleRedirect() {
            val openerWindow = window.opener as? Window ?: return
            val targetOrigin = openerWindow.location.origin
            openerWindow.postMessage(window.location.toString(), targetOrigin)
            window.close()
        }
    }
}