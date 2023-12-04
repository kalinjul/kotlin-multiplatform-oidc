package org.publicvalue.multiplatform.oidc

import io.ktor.http.HttpStatusCode
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIDConnectException", name = "OpenIDConnectException", exact = true)
sealed class OpenIDConnectException(
    override val message: String,
    override val cause: Throwable? = null
): Exception(message, cause) {

    data class InvalidUrl(val url: String?, override val cause: Throwable? = null): OpenIDConnectException(message = "Invalid URL: $url", cause = cause)
    data class AuthenticationFailure(override val message: String, override val cause: Throwable? = null): OpenIDConnectException(message = "Authentication failed. $message", cause = cause)
    data class UnsuccessfulTokenRequest(
        override val message: String,
        val statusCode: HttpStatusCode,
        val body: String?,
        val errorResponse: ErrorResponse?,
        override val cause: Throwable? = null
    ): OpenIDConnectException(message = "Authentication failed. $message", cause = cause)

    data class UnsupportedFormat(override val message: String): OpenIDConnectException(message)

    data class TechnicalFailure(override val message: String, override val cause: Throwable?): OpenIDConnectException(message, cause)
}
