@file:Suppress("Unused")
package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import kotlin.coroutines.cancellation.CancellationException

// swift convenience overloads with default parameters for suspend functions

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.exchangeToken
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun OpenIdConnectClient.exchangeToken(authCodeRequest: AuthCodeRequest, code: String) =
    exchangeToken(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.endSession
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun OpenIdConnectClient.endSession(idToken: String) =
    endSession(idToken, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.createAccessTokenRequest
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
fun OpenIdConnectClient.createAccessTokenRequest(authCodeRequest: AuthCodeRequest, code: String) =
    createAccessTokenRequest(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.createRefreshTokenRequest
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
fun OpenIdConnectClient.createRefreshTokenRequest(refreshToken: String) =
    createRefreshTokenRequest(refreshToken, null)

/**
 * Objective-C convenience function
 * @see OpenIdConnectClient.refreshToken
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun OpenIdConnectClient.refreshToken(refreshToken: String) =
    refreshToken(refreshToken, null)