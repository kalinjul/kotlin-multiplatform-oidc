package org.publicvalue.multiplatform.oidc.types

import kotlinx.serialization.Serializable

/**
 * https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1
 */
@Serializable
data class ErrorResponse(
    val error: Error,
    val error_description: String?,
    val error_uri: String?,
    val state: String?
) {
    @Serializable
    enum class Error {
        bad_verification_code,
        invalid_request,
        unauthorized_client,
        access_denied,
        unsupported_response_type,
        invalid_scope,
        server_error,
        temporarily_unavailable
    }
}