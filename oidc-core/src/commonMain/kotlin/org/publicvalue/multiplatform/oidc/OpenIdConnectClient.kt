package org.publicvalue.multiplatform.oidc

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.TokenRequest
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.remote.OpenIdConnectConfiguration
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Builds an [OpenIdConnectClientConfig] using the [block] parameter and returns an OpenID Connect
 * client.
 *
 * This uses the default ktor HTTP client. You may provide your own client using the
 * [DefaultOpenIdConnectClient] constructor.
 *
 * @param discoveryUri if set, endpoints in the configuration are optional.
 * Setting an endpoint manually will override a discovered endpoint.
 * @param block configuration closure. See [OpenIdConnectClientConfig]
 */
@Suppress("unused")
fun OpenIdConnectClient(
    discoveryUri: String? = null,
    block: OpenIdConnectClientConfig.() -> Unit
): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig(discoveryUri).apply(block)
    return DefaultOpenIdConnectClient(config = config)
}

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "OpenIdConnectClientProtocol", name = "OpenIdConnectClientProtocol", exact = true)
interface OpenIdConnectClient {
    val config: OpenIdConnectClientConfig
    val discoverDocument: OpenIdConnectConfiguration?

    /**
     * Creates an Authorization Code Request which can then be executed by the
     * [CodeAuthFlow][org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow].
     */
    fun createAuthorizationCodeRequest(configure: (URLBuilder.() -> Unit)? = null): AuthCodeRequest

    /**
     * Discover OpenID Connect Configuration using the discovery endpoint.
     * Updates the configuration, but will keep any existing configuration.
     *
     * See: [OpenID Connect Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun discover()

    /**
     * RP-initiated logout.
     * Just performs the GET request for logout, we skip the redirect part for convenience.
     *
     * See: [OpenID Spec](https://openid.net/specs/openid-connect-rpinitiated-1_0.html)
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun endSession(
        idToken: String,
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpStatusCode

    /**
     * Create and send an Access Token Request following
     * [RFC6749: OAuth](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1.3) and
     * [RFC7636: PKCE](https://datatracker.ietf.org/doc/html/rfc7636#section-4.5)
     *
     * @param authCodeRequest the original request for auth code
     * @param code the authcode received via redirect
     * @param configure configuration closure for the HTTP request
     *
     * @return [AccessTokenResponse]
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun exchangeToken(
        authCodeRequest: AuthCodeRequest,
        code: String,
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): AccessTokenResponse

    /**
     * Create and send a Refresh Token Request.
     * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-6)
     *
     * @param refreshToken the refresh token
     * @param configure configuration closure for the HTTP request
     *
     * @return [AccessTokenResponse]
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    @Suppress("Unused")
    suspend fun refreshToken(
        refreshToken: String,
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): AccessTokenResponse

    /**
     * Create an Access Token Request.
     * You should use [OpenIdConnectClient.exchangeToken] for creating and executing a request instead.
     *
     * @param authCodeRequest the original request for auth code
     * @param code the authcode received via redirect
     * @param configure configuration closure for the HTTP request
     *
     * @return [TokenRequest]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun createAccessTokenRequest(
        authCodeRequest: AuthCodeRequest,
        code: String,
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): TokenRequest

    /**
     * Create a Refresh Token Request.
     * You should use [OpenIdConnectClient.refreshToken] for creating and executing a request instead.
     *
     * @param refreshToken the refresh token
     * @param configure configuration closure for the HTTP request
     *
     * @return [TokenRequest]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    suspend fun createRefreshTokenRequest(
        refreshToken: String,
        configure: (HttpRequestBuilder.() -> Unit)? = null
    ): TokenRequest
}