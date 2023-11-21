package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.OpenIDConnectException
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.validateState

abstract class OidcAuthFlow(val client: OpenIDConnectClient) {

    suspend fun getAccessToken(): AccessTokenResponse {
        val request = client.createAuthCodeRequest()
        return getAccessToken(request)
    }

    private suspend fun getAccessToken(request: AuthCodeRequest): AccessTokenResponse {
        val codeResponse = getAccessCode(request)
        val tokenResponse = exchangeToken(client, request, codeResponse)
        return tokenResponse
    }

    abstract suspend fun getAccessCode(request: AuthCodeRequest): AuthResponse

    private suspend fun exchangeToken(client: OpenIDConnectClient, request: AuthCodeRequest, authResponse: AuthResponse): AccessTokenResponse {
        return when (authResponse) {
            is AuthResponse.CodeResponse -> {
                if (authResponse.code != null) {
                    request.validateState(authResponse.code)
                    val response = client.exchangeToken(request, authResponse.code)
                    return response
                } else {
                    throw OpenIDConnectException.AuthenticationFailed("No auth code", cause = null)
                }
            }
            is AuthResponse.ErrorResponse -> {
                throw OpenIDConnectException.AuthenticationFailed(authResponse.error ?: "", cause = null)
            }
        }
    }
}

expect class PlatformOidcAuthFlow: OidcAuthFlow