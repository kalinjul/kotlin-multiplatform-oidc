package org.publicvalue.multiplatform.oidc.tokenstore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

/**
 * Concurrency-safe Token Refresh Handler.
 */
@ExperimentalOpenIdConnect
@OptIn(ExperimentalObjCName::class, ExperimentalObjCRefinement::class)
@ObjCName("TokenRefreshHandler", "TokenRefreshHandler", exact = true)
public class TokenRefreshHandler(
    private val tokenStore: TokenStore,
) {
    private val mutex = Mutex()

    /**
     * Thread-safe refresh the tokens and save to store.
     * @return The new tokens
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    public suspend fun refreshAndSaveToken(client: OpenIdConnectClient, oldAccessToken: String): OauthTokens {
        return refreshAndSaveToken(client::refreshToken, oldAccessToken)
    }

    /**
     * Thread-safe refresh the tokens and save to store.
     *
     * @param oldAccessToken The access token that was used for the previous get request that failed with 401.
     * Required to avoid multiple refresh calls when calls return 401 simultaneously.
     *
     * @return The new access token
     */
    @Throws(OpenIdConnectException::class, CancellationException::class)
    @HiddenFromObjC
    public suspend fun refreshAndSaveToken(
        refreshCall: suspend (String) -> AccessTokenResponse,
        oldAccessToken: String
    ): OauthTokens {
        mutex.withLock {
            val currentTokens = tokenStore.getTokens()
            return if (currentTokens != null && currentTokens.accessToken != oldAccessToken) {
                currentTokens
            } else {
                val refreshToken = tokenStore.getRefreshToken()
                var newTokens = refreshCall(refreshToken ?: "")
                if (newTokens.refreshToken == null) {
                    newTokens = newTokens.copy(refreshToken = refreshToken)
                }
                tokenStore.saveTokens(newTokens)

                OauthTokens(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken,
                    idToken = newTokens.idToken
                )
            }
        }
    }
}
