package org.publicvalue.multiplatform.oidc

import io.ktor.http.HttpStatusCode
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIdConnectException", name = "OpenIdConnectException", exact = true)
sealed class OpenIdConnectException(
    override val message: String,
    override val cause: Throwable? = null
): Exception(message, cause) {

    data class InvalidUrl(val url: String?, override val cause: Throwable? = null): OpenIdConnectException(message = "Invalid URL: $url", cause = cause)
    data class AuthenticationFailure(override val message: String, override val cause: Throwable? = null): OpenIdConnectException(message = "Authentication failed. $message", cause = cause)
    data class AuthenticationCancelled(override val message: String = "Authentication cancelled"): OpenIdConnectException(message = "Authentication cancelled", cause = null)
    data class UnsuccessfulTokenRequest(
        override val message: String,
        val statusCode: HttpStatusCode,
        val body: String?,
        val errorResponse: ErrorResponse?,
        override val cause: Throwable? = null
    ): OpenIdConnectException(message = "Authentication failed. $message", cause = cause)

    data class UnsupportedFormat(override val message: String): OpenIdConnectException(message)

    data class TechnicalFailure(override val message: String, override val cause: Throwable?): OpenIdConnectException(message, cause)

    data class InvalidConfiguration(override val message: String): OpenIdConnectException(message)
}
