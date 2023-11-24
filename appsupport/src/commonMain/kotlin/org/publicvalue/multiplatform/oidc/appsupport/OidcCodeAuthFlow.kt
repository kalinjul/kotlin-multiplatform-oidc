package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.OpenIDConnectException
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.validateState

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1
 *
 * Implementations have to provide their own method to get the authorization code,
 * as this requires user interaction (e.g. via browser).
 */
abstract class OidcCodeAuthFlow(val client: OpenIDConnectClient) {

    suspend fun getAccessToken(): AccessTokenResponse {
        val request = client.createAuthorizationCodeRequest()
        return getAccessToken(request)
    }

    private suspend fun getAccessToken(request: AuthCodeRequest): AccessTokenResponse {
        val codeResponse = getAuthorizationCode(request)
        return exchangeToken(client, request, codeResponse)
    }

    /**
     * Uses the request URL to open a browser and perform authorization.
     * Should return the Authorization Code.
     */
    abstract suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthResponse

    private suspend fun exchangeToken(client: OpenIDConnectClient, request: AuthCodeRequest, authResponse: AuthResponse): AccessTokenResponse {
        return when (authResponse) {
            is AuthResponse.CodeResponse -> {
                if (authResponse.code != null) {
                    if (!request.validateState(authResponse.state ?: "")) {
                        throw OpenIDConnectException.AuthenticationFailed("Invalid state")
                    }
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

expect class PlatformOidcCodeAuthFlow: OidcCodeAuthFlow