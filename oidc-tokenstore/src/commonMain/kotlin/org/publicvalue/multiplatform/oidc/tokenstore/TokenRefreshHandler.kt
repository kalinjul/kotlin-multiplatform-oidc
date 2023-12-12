package org.publicvalue.multiplatform.oidc.tokenstore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Concurrency-safe Token Refresh Handler.
 */
@ExperimentalOpenIdConnect
@OptIn(ExperimentalObjCName::class)
@ObjCName("TokenRefreshHandler", "TokenRefreshHandler", exact = true)
@Suppress("unused")
class TokenRefreshHandler(
    private val tokenStore: TokenStore,
) {
    private val mutex = Mutex()

    /**
     * Thread-safe refresh the tokens and save to store.
     * @return The new access token
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun safeRefreshToken(client: OpenIdConnectClient, oldAccessToken: String): String {
        return safeRefreshToken(client::refreshToken, oldAccessToken)
    }

    /**
     * Thread-safe refresh the tokens and save to store.
     * @return The new access token
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun safeRefreshToken(refreshCall: suspend (String) -> AccessTokenResponse, oldAccessToken: String): String {
        mutex.withLock {
            val currentToken = tokenStore.getAccessToken()
            return if (currentToken != null && currentToken != oldAccessToken) {
                currentToken
            } else {
                val refreshToken = tokenStore.getRefreshToken()
                val newTokens = refreshCall(refreshToken ?: "")
                tokenStore.saveTokens(newTokens)
                newTokens.access_token
            }
        }
    }
}