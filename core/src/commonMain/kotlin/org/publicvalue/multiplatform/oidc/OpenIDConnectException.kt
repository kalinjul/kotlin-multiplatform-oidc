package org.publicvalue.multiplatform.oidc

import io.ktor.http.HttpStatusCode


sealed class OpenIDConnectException(
    override val message: String,
    override val cause: Throwable? = null
): Exception(message, cause) {

    data class InvalidUrl(val url: String?, override val cause: Throwable? = null): OpenIDConnectException(message = "Invalid URL: $url", cause = cause)
    data class AuthenticationFailed(override val message: String, override val cause: Throwable? = null): OpenIDConnectException(message = "Authentication failed. $message", cause = cause)
    data class UnsuccessfulTokenRequest(
        override val message: String,
        val statusCode: HttpStatusCode,
        val body: String?,
        override val cause: Throwable? = null
    ): OpenIDConnectException(message = "Authentication failed. $message", cause = cause)
}