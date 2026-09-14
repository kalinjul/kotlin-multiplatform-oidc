package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.URLProtocol
import io.ktor.http.Url
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
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
import platform.AuthenticationServices.ASWebAuthenticationSessionCallback
import platform.Foundation.NSError
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
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
                val completionHandler: (NSURL?, NSError?) -> Unit = { url, _ ->
                    if (url != null) {
                        val url = Url(url.toString()) // use sane url instead of NS garbage
                        runBlocking {
                            preferences.setResponseUri(url)
                        }
                        continuation.resumeIfActive(WebAuthenticationFlowResult.Success(url))
                    } else {
                        // browser closed, no redirect.
                        continuation.resumeIfActive(WebAuthenticationFlowResult.Cancelled)
                    }
                }

                val redirect = Url(redirectUrl)
                val httpsCallback = redirect.httpsCallbackOrNull()
                val session = if (httpsCallback != null) {
                    ASWebAuthenticationSession(
                        uRL = nsurl,
                        callback = httpsCallback,
                        completionHandler = completionHandler
                    )
                } else {
                    ASWebAuthenticationSession(
                        uRL = nsurl,
                        callbackURLScheme = redirect.protocol.name,
                        completionHandler = completionHandler
                    )
                }
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

/** ASWebAuthenticationSession callback matching was introduced in iOS 17.4. */
@OptIn(ExperimentalForeignApi::class)
internal val supportsCallbackMatching: Boolean by lazy {
    NSProcessInfo.processInfo.isOperatingSystemAtLeastVersion(
        cValue<NSOperatingSystemVersion> {
            majorVersion = 17
            minorVersion = 4
            patchVersion = 0
        }
    )
}

/**
 * Callback matcher for https redirect uris.
 *
 * ASWebAuthenticationSession's `callbackURLScheme` parameter only accepts private-use schemes, so
 * an https redirect uri never completes the flow. Returns null when the redirect uri is not https
 * or the OS predates callback matching, letting the caller fall back to scheme matching.
 */
internal fun Url.httpsCallbackOrNull(): ASWebAuthenticationSessionCallback? {
    if (protocol != URLProtocol.HTTPS || !supportsCallbackMatching) return null
    return ASWebAuthenticationSessionCallback.callbackWithHTTPSHost(host = host, path = encodedPath)
}

private class PresentationContext: NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor {
        return ASPresentationAnchor()
    }
}