package org.publicvalue.multiplatform.oidc.flows

import io.ktor.client.request.HttpRequestBuilder
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.AuthRequest
import org.publicvalue.multiplatform.oidc.types.remote.AuthResponse
import org.publicvalue.multiplatform.oidc.types.remote.AuthResult
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1)
 *
 * Implementations have to provide their own method [getAuthorizationResult]
 * as this requires user interaction (e.g. via browser).
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AbstractCodeAuthFlow", name = "AbstractCodeAuthFlow", exact = true)
abstract class AuthFlow(val client: OpenIdConnectClient) {

    /**
     * For some reason the default parameter is not available in Platform implementations,
     * so this provides an empty parameter method instead.
     */
    @Suppress("unused")
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun getAccessToken(): AuthResult.AccessToken = getAccessToken(Type.Code, null)

    /**
     * Start the authorization flow to request an access token.
     *
     * @param configure configuration closure to configure the http request builder with (will _not_
     * be used for discovery if necessary)
     */
    @Suppress("unused")
    @Throws(CancellationException::class, OpenIdConnectException::class)
    suspend fun getAccessToken(
        type: Type = Type.Code,
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): AuthResult.AccessToken = wrapExceptions {
        if (!client.config.discoveryUri.isNullOrEmpty()) {
            client.discover()
        }
        val request = when (type) {
            is Type.Code -> client.createAuthorizationCodeRequest()
            is Type.Implicit -> client.createImplicitAccessTokenRequest()
        }
        return getAccessToken(request, configure)
    }

    private suspend fun getAccessToken(request: AuthRequest, configure: (HttpRequestBuilder.() -> Unit)?): AuthResult.AccessToken {
        val codeResponse = getAuthorizationResult(request)
        return exchangeToken(client, request, codeResponse, configure)
    }

    /**
     * Uses the request URL to open a browser and perform authorization.
     * @param request The request containing the url and relevant state information
     * @return the Authorization Code.
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    abstract suspend fun getAuthorizationResult(request: AuthRequest): AuthResponse

    private suspend fun exchangeToken(
        client: OpenIdConnectClient,
        request: AuthRequest,
        authCodeResponse: AuthResponse,
        configure: (HttpRequestBuilder.() -> Unit)?
    ): AuthResult.AccessToken {
        authCodeResponse.fold(
            onSuccess = { result ->
                return when (result) {
                    is AuthResult.Code -> {
                        if (result.code != null) {
                            if (!request.validate(result.state ?: "")) {
                                throw OpenIdConnectException.AuthenticationFailure("Invalid state")
                            }
                            val response = client.exchangeToken(request, result.code, configure)
                            return response
                        } else {
                            throw OpenIdConnectException.AuthenticationFailure("No auth code", cause = null)
                        }
                    }
                    is AuthResult.AccessToken -> result
                }
            },
            onFailure = {
                throw OpenIdConnectException.AuthenticationFailure("AuthCode response was error: ${it.message}", cause = it)
            }
        )
    }

    sealed interface Type {
        data object Code : Type
        data object Implicit : Type
    }
}