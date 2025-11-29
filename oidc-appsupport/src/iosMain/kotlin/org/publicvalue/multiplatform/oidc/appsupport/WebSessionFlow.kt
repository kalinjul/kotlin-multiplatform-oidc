package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.setResponseUri
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionCompletionHandler
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject

internal class WebSessionFlow(
    private val ephemeralBrowserSession: Boolean,
    private val preferences: Preferences,
): WebAuthenticationFlow {
    /**
     * @return null if user cancelled the flow (closed the web view)
     */
    override suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): WebAuthenticationFlowResult {
        return suspendCancellableCoroutine { continuation ->
            val nsurl = NSURL.URLWithString(requestUrl.toString())
            if (nsurl != null) {
                val session = ASWebAuthenticationSession(
                    uRL = nsurl,
                    callbackURLScheme = Url(redirectUrl).protocol.name,
                    completionHandler = object : ASWebAuthenticationSessionCompletionHandler {
                        override fun invoke(p1: NSURL?, p2: NSError?) {
                            if (p1 != null) {
                                val url = Url(p1.toString()) // use sane url instead of NS garbage
                                runBlocking {
                                    preferences.setResponseUri(url)
                                }
                                continuation.resumeIfActive(WebAuthenticationFlowResult.Success(url))
                            } else {
                                // browser closed, no redirect.
                                continuation.resumeIfActive(WebAuthenticationFlowResult.Cancelled)
                            }
                        }
                    }
                )
                session.prefersEphemeralWebBrowserSession = ephemeralBrowserSession
                session.presentationContextProvider = PresentationContext()

                MainScope().launch {
                    session.start()
                }
            } else {
                continuation.resumeWithExceptionIfActive(OpenIdConnectException.InvalidUrl(requestUrl.toString()))
            }
        }
    }
}

private class PresentationContext: NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor {
        return ASPresentationAnchor()
    }
}