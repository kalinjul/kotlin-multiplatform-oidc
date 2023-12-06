package org.publicvalue.multiplatform.oidc.appsupport

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

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
    suspend fun safeRefreshToken(client: OpenIdConnectClient): String {
        return safeRefreshToken({ client.refreshToken(it) })
    }

    /**
     * Thread-safe refresh the tokens and save to store.
     * @return The new access token
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    suspend fun safeRefreshToken(refreshCall: suspend (String) -> AccessTokenResponse): String {
        val oldToken = tokenStore.getAccessToken()
        mutex.withLock {
            val currentToken = tokenStore.getAccessToken()
            return if (currentToken != null && currentToken != oldToken) {
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