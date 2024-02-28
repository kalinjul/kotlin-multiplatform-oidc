@file:Suppress("Unused")
package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import kotlin.coroutines.cancellation.CancellationException

// swift convenience overloads with default parameters for suspend functions
// defined on DefaultOpenIdConnectClient because we cannot export extensions for interfaces yet

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.exchangeToken
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun DefaultOpenIdConnectClient.exchangeToken(authCodeRequest: AuthCodeRequest, code: String) =
    exchangeToken(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.endSession
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun DefaultOpenIdConnectClient.endSession(idToken: String) =
    endSession(idToken, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.createAccessTokenRequest
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun DefaultOpenIdConnectClient.createAccessTokenRequest(authCodeRequest: AuthCodeRequest, code: String) =
    createAccessTokenRequest(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.createRefreshTokenRequest
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun DefaultOpenIdConnectClient.createRefreshTokenRequest(refreshToken: String) =
    createRefreshTokenRequest(refreshToken, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.refreshToken
 * @suppress
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun DefaultOpenIdConnectClient.refreshToken(refreshToken: String) =
    refreshToken(refreshToken, null)