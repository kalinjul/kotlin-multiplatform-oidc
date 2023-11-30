package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.OpenIDConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.OidcCodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
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

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PlatformOidcCodeAuthFlow", name = "PlatformOidcCodeAuthFlow", exact = true)
actual class PlatformOidcCodeAuthFlow(
    client: OpenIDConnectClient
): OidcCodeAuthFlow(client) {
    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        val authResponse = suspendCoroutine { continuation ->
            val nsurl = NSURL.URLWithString(request.url.toString())
            if (nsurl != null) {
                val session = ASWebAuthenticationSession(
                    uRL = nsurl,
                    callbackURLScheme = "org.publicvalue.multiplatform.oidc.sample",
                    completionHandler = object : ASWebAuthenticationSessionCompletionHandler {
                        override fun invoke(p1: NSURL?, p2: NSError?) {
                            if (p1 != null) {
                                val url = Url(p1.toString()) // use sane url instead of NS garbage
                                val code = url.parameters["code"] ?: ""
                                val state = url.parameters["state"] ?: ""

                                continuation.resume(AuthCodeResponse.success(AuthCodeResult(code = code, state = state)))
                            } else {
                                if (p2 != null) {
                                    continuation.resume(AuthCodeResponse.failure<AuthCodeResult>(OpenIDConnectException.AuthenticationFailed(p2.localizedDescription)))
                                } else {
                                    continuation.resume(AuthCodeResponse.failure<AuthCodeResult>(OpenIDConnectException.AuthenticationFailed("No message")))
                                }

                            }
                        }
                    }
                )
                session.prefersEphemeralWebBrowserSession = true
                session.presentationContextProvider = PresentationContext()

                MainScope().launch {
                    session.start()
                }
            } else {
                continuation.resumeWithException(OpenIDConnectException.InvalidUrl(request.url.toString()))
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