package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * ErrorResponse expected from Authorization or Token endpoint.
 * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1)
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIdConnectErrorResponse", name = "OpenIdConnectErrorResponse", exact = true)
@Serializable
data class ErrorResponse(
    val error: Error,
    val error_description: String?,
    val error_uri: String?,
    val state: String?
) {
    @Serializable
    enum class Error {
        invalid_client,
        invalid_grant,
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
