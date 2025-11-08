@file:Suppress("ForbiddenPublicDataClass")

package org.publicvalue.multiplatform.oidc

import io.ktor.http.HttpStatusCode
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIdConnectException", name = "OpenIdConnectException", exact = true)
public sealed class OpenIdConnectException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    public data class InvalidUrl(val url: String?, override val cause: Throwable? = null) :
        OpenIdConnectException(
            message = "Invalid URL: $url",
            cause = cause
        )

    public data class AuthenticationFailure(
        override val message: String,
        override val cause: Throwable? = null
    ) : OpenIdConnectException(message = "Authentication failed. $message", cause = cause)

    public data class AuthenticationCancelled(
        override val message: String = "Authentication cancelled"
    ) : OpenIdConnectException(message = "Authentication cancelled", cause = null)

    public data class UnsuccessfulTokenRequest(
        override val message: String,
        val statusCode: HttpStatusCode,
        val body: String?,
        val errorResponse: ErrorResponse?,
        override val cause: Throwable? = null
    ) : OpenIdConnectException(message = "Authentication failed. $message", cause = cause)

    public data class UnsupportedFormat(override val message: String) :
        OpenIdConnectException(message)

    public data class TechnicalFailure(
        override val message: String,
        override val cause: Throwable?
    ) : OpenIdConnectException(
        message,
        cause
    )

    public data class InvalidConfiguration(override val message: String) :
        OpenIdConnectException(message)
}
