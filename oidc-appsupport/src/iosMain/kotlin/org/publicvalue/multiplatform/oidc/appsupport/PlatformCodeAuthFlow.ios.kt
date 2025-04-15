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
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
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
): CodeAuthFlow {

    // required for swift (no default argument support)
    constructor(client: OpenIdConnectClient) : this(client = client, ephemeralBrowserSession = false)

    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse = wrapExceptions {
        val authResponse = suspendCancellableCoroutine { continuation ->
            val nsurl = NSURL.URLWithString(request.url.toString())
            if (nsurl != null) {
                val session = ASWebAuthenticationSession(
                    uRL = nsurl,
                    callbackURLScheme = request.config.redirectUri?.let { Url(it) }?.protocol?.name,
                    completionHandler = object : ASWebAuthenticationSessionCompletionHandler {
                        override fun invoke(p1: NSURL?, p2: NSError?) {
                            if (p1 != null) {
                                val url = Url(p1.toString()) // use sane url instead of NS garbage
                                val code = url.parameters["code"] ?: ""
                                val state = url.parameters["state"] ?: ""

                                continuation.resumeIfActive(AuthCodeResponse.success(AuthCodeResult(code = code, state = state)))
                            } else {
                                // browser closed, no redirect.
                                continuation.resumeIfActive(AuthCodeResponse.failure<AuthCodeResult>(OpenIdConnectException.AuthenticationCancelled(p2?.localizedDescription()?: "Authentication cancelled"))                                    )
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
                if (continuation.isActive) {
                    continuation.resumeWithExceptionIfActive(OpenIdConnectException.InvalidUrl(request.url.toString()))
                }
            }
        }

        return authResponse
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