@file:Suppress("Unused")
package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import kotlin.coroutines.cancellation.CancellationException

// swift convenience overloads with default parameters for suspend functions

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.exchangeToken
 */
@Throws(OpenIDConnectException::class, CancellationException::class)
suspend fun OpenIDConnectClient.exchangeToken(authCodeRequest: AuthCodeRequest, code: String) =
    exchangeToken(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.endSession
 */
@Throws(OpenIDConnectException::class, CancellationException::class)
suspend fun OpenIDConnectClient.endSession(idToken: String) =
    endSession(idToken, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.createAccessTokenRequest
 */
@Throws(OpenIDConnectException::class, CancellationException::class)
fun OpenIDConnectClient.createAccessTokenRequest(authCodeRequest: AuthCodeRequest, code: String) =
    createAccessTokenRequest(authCodeRequest, code, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.createRefreshTokenRequest
 */
@Throws(OpenIDConnectException::class, CancellationException::class)
fun OpenIDConnectClient.createRefreshTokenRequest(refreshToken: String) =
    createRefreshTokenRequest(refreshToken, null)

/**
 * Objective-C convenience function
 * @see OpenIDConnectClient.refreshToken
 */
@Throws(OpenIDConnectException::class, CancellationException::class)
suspend fun OpenIDConnectClient.refreshToken(refreshToken: String) =
    refreshToken(refreshToken, null)