package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * ErrorResponse expected from Authorization or Token endpoint.
 * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.2.1)
 * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-5.2)
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIdConnectErrorResponse", name = "OpenIdConnectErrorResponse", exact = true)
@Serializable
public data class ErrorResponse(
    val error: Error,
    @SerialName("error_description")
    val errorDescription: String?,
    @SerialName("error_uri")
    val errorUri: String?,
    val state: String?
) {
    @Serializable
    public enum class Error {
        @SerialName("invalid_client")
        INVALID_CLIENT,

        @SerialName("invalid_grant")
        INVALID_GRANT,

        @SerialName("bad_verification_code")
        BAD_VERIFICATION_CODE,

        @SerialName("invalid_request")
        INVALID_REQUEST,

        @SerialName("unauthorized_client")
        UNAUTHORIZED_CLIENT,

        @SerialName("unsupported_grant_type")
        UNSUPPORTED_GRANT_TYPE,

        @SerialName("access_denied")
        ACCESS_DENIED,

        @SerialName("unsupported_response_type")
        UNSUPPORTED_RESPONSE_TYPE,

        @SerialName("invalid_scope")
        INVALID_SCOPE,

        @SerialName("server_error")
        SERVER_ERROR,

        @SerialName("temporarily_unavailable")
        TEMPORARILY_UNAVAILABLE
    }
}
