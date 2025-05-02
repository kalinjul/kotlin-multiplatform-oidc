package org.publicvalue.multiplatform.oauth.domain.types

import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

sealed class AuthorizeResult {
    data class Request(
        val authCodeRequestUrl: String,
        val authCodeRequest: AuthCodeRequest
    ): AuthorizeResult()
    data class Response(
        val authCode: String,
        val authCodeResponseQueryString: String
    ): AuthorizeResult()
}