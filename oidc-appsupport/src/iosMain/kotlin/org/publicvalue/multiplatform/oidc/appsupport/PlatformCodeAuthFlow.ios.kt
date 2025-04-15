package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import org.publicvalue.multiplatform.oidc.wrapExceptions
import platform.AuthenticationServices.ASPresentationAnchor
import platform.AuthenticationServices.ASWebAuthenticationPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASWebAuthenticationSession
import platform.AuthenticationServices.ASWebAuthenticationSessionCompletionHandler
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.experimental.ExperimentalObjCName

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1
 *
 * Implementations have to provide their own method to get the authorization code,
 * as this requires user interaction (e.g. via browser).
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "CodeAuthFlow", name = "CodeAuthFlow", exact = true)
actual class PlatformCodeAuthFlow(
    override val client: OpenIdConnectClient,
    private val ephemeralBrowserSession: Boolean = false
): CodeAuthFlow, EndSessionFlow {

    // required for swift (no default argument support)
    constructor(client: OpenIdConnectClient) : this(client = client, ephemeralBrowserSession = false)

    /**
     * @return null if user cancelled the flow (closed the web view)
     */
    private suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): Url? {
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
                                continuation.resumeIfActive(url)
                            } else {
                                // browser closed, no redirect.
                                continuation.resumeIfActive(null)
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

    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse = wrapExceptions {
        val resultUrl = startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())
        return if (resultUrl != null) {
            val code = resultUrl.parameters["code"] ?: ""
            val state = resultUrl.parameters["state"] ?: ""
            AuthCodeResponse.success(AuthCodeResult(code = code, state = state))
        } else {
            AuthCodeResponse.failure(OpenIdConnectException.AuthenticationCancelled("Authentication cancelled"))
        }
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse = wrapExceptions {
        val resultUrl = startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())
        return if (resultUrl != null) {
            EndSessionResponse.success(Unit)
        } else {
            EndSessionResponse.failure(OpenIdConnectException.AuthenticationCancelled("Logout cancelled"))
        }
    }
}

class PresentationContext: NSObject(), ASWebAuthenticationPresentationContextProvidingProtocol {
    override fun presentationAnchorForWebAuthenticationSession(session: ASWebAuthenticationSession): ASPresentationAnchor {
        return ASPresentationAnchor()
    }
}

/** fix for multiple callbacks from ASWebAuthenticationSession (https://github.com/kalinjul/kotlin-multiplatform-oidc/issues/89) **/
fun <T> CancellableContinuation<T>.resumeIfActive(value: T) {
    if (isActive) {
        resume(value)
    }
}

/** fix for multiple callbacks from ASWebAuthenticationSession (https://github.com/kalinjul/kotlin-multiplatform-oidc/issues/89) **/
fun <T> CancellableContinuation<T>.resumeWithExceptionIfActive(value: Exception) {
    if (isActive) {
        resumeWithException(value)
    }
}