package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
import kotlin.coroutines.suspendCoroutine
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
    client: OpenIdConnectClient,
    private val ephemeralBrowserSession: Boolean = false
): CodeAuthFlow(client) {

    // required for swift (no default argument support)
    constructor(client: OpenIdConnectClient) : this(client = client, ephemeralBrowserSession = false)

    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse = wrapExceptions {
        val authResponse = suspendCoroutine { continuation ->
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

                                continuation.resume(AuthCodeResponse.success(AuthCodeResult(code = code, state = state)))
                            } else {
                                if (p2 != null) {
                                    continuation.resume(AuthCodeResponse.failure<AuthCodeResult>(OpenIdConnectException.AuthenticationFailure(p2.localizedDescription)))
                                } else {
                                    continuation.resume(AuthCodeResponse.failure<AuthCodeResult>(OpenIdConnectException.AuthenticationFailure("No message")))
                                }
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
                continuation.resumeWithException(OpenIdConnectException.InvalidUrl(request.url.toString()))
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