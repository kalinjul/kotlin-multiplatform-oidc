package org.publicvalue.multiplatform.oidc.appsupport

import kotlinx.coroutines.CancellableContinuation
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
actual class PlatformCodeAuthFlow internal constructor(
    actual override val client: OpenIdConnectClient,
    ephemeralBrowserSession: Boolean = false,
    private val webFlow: WebAuthenticationFlow,
): CodeAuthFlow, EndSessionFlow {

    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse = wrapExceptions {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("redirect_uri").orEmpty())
        return if (result is WebAuthenticationFlowResult.Success) {
            when (val error = getErrorResult<AuthCodeResult>(result.responseUri)) {
                null -> {
                    val state = result.responseUri?.parameters?.get("state")
                    val code = result.responseUri?.parameters?.get("code")
                    Result.success(AuthCodeResult(code, state))
                }
                else -> {
                    return error
                }
            }
        } else {
            // browser closed, no redirect
            Result.failure(OpenIdConnectException.AuthenticationCancelled())
        }
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse = wrapExceptions {
        val result = webFlow.startWebFlow(request.url, request.url.parameters.get("post_logout_redirect_uri").orEmpty())

        return if (result is WebAuthenticationFlowResult.Success) {
            when (val error = getErrorResult<Unit>(result.responseUri)) {
                null -> {
                    return EndSessionResponse.success(Unit)
                }
                else -> {
                    return error
                }
            }
        } else {
            // browser closed, no redirect
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