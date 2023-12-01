package org.publicvalue.multiplatform.oidc.flows

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.OpenIDConnectException
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.validateState
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: https://datatracker.ietf.org/doc/html/rfc6749#section-4.1
 *
 * Implementations have to provide their own method to get the authorization code,
 * as this requires user interaction (e.g. via browser).
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AbstractOidcCodeAuthFlow", name = "AbstractOidcCodeAuthFlow", exact = true)
abstract class OidcCodeAuthFlow(val client: OpenIDConnectClient) {

    @Suppress("unused")
    suspend fun getAccessToken(): AccessTokenResponse {
        if (client.config.discoveryUri != null) {
            client.discover()
        }
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
    abstract suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse

    private suspend fun exchangeToken(client: OpenIDConnectClient, request: AuthCodeRequest, authCodeResponse: AuthCodeResponse): AccessTokenResponse {
        authCodeResponse.fold(
            onSuccess = {
                if (it.code != null) {
                    if (!request.validateState(it.state ?: "")) {
                        throw OpenIDConnectException.AuthenticationFailure("Invalid state")
                    }
                    val response = client.exchangeToken(request, it.code)
                    return response
                } else {
                    throw OpenIDConnectException.AuthenticationFailure("No auth code", cause = null)
                }
            },
            onFailure = {
                throw OpenIDConnectException.AuthenticationFailure("AuthCode response was error: ${it.message}", cause = it)
            }
        )
    }
}