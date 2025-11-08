@file:Suppress("Unused")

package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.TokenRequest
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.coroutines.cancellation.CancellationException

// swift convenience overloads with default parameters for public suspend functions
// defined on DefaultOpenIdConnectClient because we cannot export extensions for interfaces yet

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.exchangeToken
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun DefaultOpenIdConnectClient.discover(): Unit =
    discover(null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.exchangeToken
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun DefaultOpenIdConnectClient.exchangeToken(
    authCodeRequest: AuthCodeRequest,
    code: String
): AccessTokenResponse =
    exchangeToken(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.endSession
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun DefaultOpenIdConnectClient.endSession(idToken: String): HttpStatusCode =
    endSession(idToken, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.createAccessTokenRequest
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun DefaultOpenIdConnectClient.createAccessTokenRequest(
    authCodeRequest: AuthCodeRequest,
    code: String
): TokenRequest =
    createAccessTokenRequest(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.createRefreshTokenRequest
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun DefaultOpenIdConnectClient.createRefreshTokenRequest(refreshToken: String): TokenRequest =
    createRefreshTokenRequest(refreshToken, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.refreshToken
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun DefaultOpenIdConnectClient.refreshToken(refreshToken: String): AccessTokenResponse =
    refreshToken(refreshToken, null)

/**
 * Create and install a custom client plugin.
 * @suppress
 */
public fun HttpClientConfig<*>.installClientPlugin(
    name: String,
    onRequest: (HttpRequestBuilder, Any) -> Unit = { _, _ -> },
    onResponse: (HttpResponse) -> Unit = {},
    onClose: () -> Unit = {},
) {
    val plugin = createClientPlugin(
        name,
        body = {
            onRequest { request, content ->
                onRequest(request, content)
            }
            onResponse {
                onResponse(it)
            }
            onClose {
                onClose()
            }
        }
    )
    install(plugin)
}
