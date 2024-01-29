package org.publicvalue.multiplatform.oidc.ktor

import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.removeTokens

/**
 * Configure Bearer Authentication using TokenStore + RefreshHandler.
 */
@ExperimentalOpenIdConnect
fun Auth.oidcBearer(
    tokenStore: TokenStore,
    refreshHandler: TokenRefreshHandler,
    client: OpenIdConnectClient,
    onRefreshFailed: suspend (Exception) -> Unit = { tokenStore.removeTokens() }
) {
    oidcBearer(
        tokenStore = tokenStore,
        refreshAndSaveTokens = { refreshHandler.safeRefreshToken(client = client, it) },
        onRefreshFailed = onRefreshFailed
    )
}

/**
 * Configure Bearer Authentication using refresh callback.
 * When using this, callers must take care of saving the token themselves inside the refresh
 * callback.
 *
 * The tokenStore is only used to retrieve tokens.
 *
 * @param refreshAndSaveTokens Callback that is used to refresh the token. Receives the old token and should
 * save it into the store.
 */
@ExperimentalOpenIdConnect
fun Auth.oidcBearer(
    tokenStore: TokenStore,
    refreshAndSaveTokens: suspend (String) -> Unit, // receives the old access token
    /** called when refresh throws **/
    onRefreshFailed: suspend (Exception) -> Unit
) {

    bearer {
        loadTokens {
            val accessToken = tokenStore.getAccessToken()
            val refreshToken = tokenStore.getRefreshToken()
            val bearer = accessToken?.let {
                BearerTokens(
                    accessToken = it,
                    refreshToken = refreshToken ?: "",
                )
            } ?: BearerTokens(
                accessToken = "",
                refreshToken = ""
            )
            bearer
        }

        refreshTokens {
            try {
                refreshAndSaveTokens(this.oldTokens?.accessToken.orEmpty())
            } catch (e: OpenIdConnectException) {
                if (e is OpenIdConnectException.UnsuccessfulTokenRequest) {
                    onRefreshFailed(e)
                }
            }
            val accessToken = tokenStore.getAccessToken()
            val refreshToken = tokenStore.getRefreshToken()
            val bearer = accessToken?.let {
                BearerTokens(
                    accessToken = it,
                    refreshToken = refreshToken ?: "",
                )
            } ?: BearerTokens(
                accessToken = "",
                refreshToken = ""
            )

            bearer
        }
    }
}