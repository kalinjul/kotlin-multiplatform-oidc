package org.publicvalue.multiplatform.oidc.flows

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLBuilder
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.parseJwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.validateNonce
import org.publicvalue.multiplatform.oidc.types.validateState
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Implements the OAuth 2.0 Code Authorization Flow.
 * See: [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1)
 *
 * Implementations have to provide their own method [getAuthorizationCode]
 * as this requires user interaction (e.g. via browser).
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AbstractCodeAuthFlow", name = "AbstractCodeAuthFlow", exact = true)
public interface CodeAuthFlow {
    public val client: OpenIdConnectClient

    /**
     * For some reason the default parameter is not available in Platform implementations,
     * so this provides an empty parameter method instead.
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    public suspend fun getAccessToken(): AccessTokenResponse = getAccessToken(null, null)

    /**
     * Start the authorization flow to request an access token.
     *
     * @param configure configuration closure to configure the http request builder with (will _not_
     * be used for discovery if necessary)
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    @Deprecated(
        message = "Use getAccessToken(configureAuthUrl, configureTokenExchange) instead",
        replaceWith = ReplaceWith("getAccessToken(configureAuthUrl = null, configureTokenExchange = configure)")
    )
    public suspend fun getAccessToken(
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): AccessTokenResponse = getAccessToken(
        null,
        configure
    )

    /**
     * Start the authorization flow to request an access token.
     *
     * @param configureAuthUrl configuration closure to configure the auth url passed to browser
     * @param configureTokenExchange configuration closure to configure the http request builder with (will _not_
     * be used for discovery if necessary)
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    public suspend fun getAccessToken(
        configureAuthUrl: (URLBuilder.() -> Unit)? = null,
        configureTokenExchange: (HttpRequestBuilder.() -> Unit)? = null
    ): AccessTokenResponse = wrapExceptions {
        if (!client.config.discoveryUri.isNullOrEmpty()) {
            client.discover()
        }
        val request = client.createAuthorizationCodeRequest(configureAuthUrl)
        return getAccessToken(request, configureTokenExchange)
    }

    private suspend fun getAccessToken(
        request: AuthCodeRequest,
        configure: (HttpRequestBuilder.() -> Unit)?
    ): AccessTokenResponse {
        val codeResponse = getAuthorizationCode(request)
        return codeResponse.fold(
            onSuccess = {
                exchangeToken(
                    client = client,
                    request = request,
                    result = it,
                    configure = configure
                )
            },
            onFailure = {
                throw it
            }
        )
    }

    /**
     * Uses the request URL to open a browser and perform authorization.
     * @param request The request containing the url and relevant state information
     * @return the Authorization Code.
     */
    @Throws(CancellationException::class, OpenIdConnectException::class)
    public suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse

    private suspend fun exchangeToken(
        client: OpenIdConnectClient,
        request: AuthCodeRequest,
        result: AuthCodeResult,
        configure: (HttpRequestBuilder.() -> Unit)?
    ): AccessTokenResponse {
        if (result.code != null) {
            if (!request.validateState(result.state ?: "")) {
                throw OpenIdConnectException.AuthenticationFailure("Invalid state")
            }
            val response = client.exchangeToken(request, result.code, configure)
            val nonce = response.idToken?.parseJwt()?.payload?.nonce
            if (!request.validateNonce(nonce ?: "")) {
                throw OpenIdConnectException.AuthenticationFailure("Invalid nonce")
            }
            return response
        } else {
            throw OpenIdConnectException.AuthenticationFailure("No auth code", cause = null)
        }
    }
}
