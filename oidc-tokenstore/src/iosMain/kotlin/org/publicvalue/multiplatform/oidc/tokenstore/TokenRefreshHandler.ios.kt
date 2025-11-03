package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.coroutines.cancellation.CancellationException

/**
 * Thread-safe refresh the tokens and save to store.
 * (convenience method to allow calls from iOS as lambdas are not supported yet)
 *
 * @return The new access token
 */
@OptIn(ExperimentalOpenIdConnect::class)
@Throws(OpenIdConnectException::class, CancellationException::class)
public suspend fun TokenRefreshHandler.refreshAndSaveToken(
    refresher: TokenRefresher,
    oldAccessToken: String
): OauthTokens {
    return refreshAndSaveToken(
        refreshCall = { refreshToken ->
            refresher.refreshToken(refreshToken = refreshToken)
        },
        oldAccessToken = oldAccessToken
    )
}

/**
 * For iOS because we cannot accept lambdas as parameters yet
 */
public interface TokenRefresher {
    public suspend fun refreshToken(refreshToken: String): AccessTokenResponse
}
