package org.publicvalue.multiplatform.oidc.appsupport

sealed class AuthResponse {
    data class CodeResponse(val code: String?, val state: String?): AuthResponse()
    data class ErrorResponse(val error: String?): AuthResponse()
}