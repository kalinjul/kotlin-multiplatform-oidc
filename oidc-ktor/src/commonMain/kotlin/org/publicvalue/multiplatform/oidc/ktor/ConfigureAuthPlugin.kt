package org.publicvalue.multiplatform.oidc.ktor

import io.ktor.client.plugins.auth.AuthConfig
import io.ktor.client.plugins.auth.providers.BearerAuthConfig
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.tokenstore.OauthTokens
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.removeTokens

/**
 * Configure Bearer Authentication using TokenStore + RefreshHandler.
 */
@ExperimentalOpenIdConnect
public fun AuthConfig.oidcBearer(
    tokenStore: TokenStore,
    refreshHandler: TokenRefreshHandler,
    client: OpenIdConnectClient,
    onRefreshFailed: suspend (Exception) -> Unit = { tokenStore.removeTokens() }
) {
    oidcBearer(
        tokenStore = tokenStore,
        refreshAndSaveTokens = { refreshHandler.refreshAndSaveToken(client = client, it) },
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
public fun AuthConfig.oidcBearer(
    tokenStore: TokenStore,
    /** receives the old access token as parameter.
     *  This function should get new tokens and save them.
     **/
    refreshAndSaveTokens: suspend (String) -> OauthTokens?,
    /** called when refresh throws **/
    onRefreshFailed: suspend (Exception) -> Unit
) {
    bearer {
        loadTokens(
            tokenStore = tokenStore
        )

        refreshTokens(
            refreshAndSaveTokens = refreshAndSaveTokens,
            onRefreshFailed = onRefreshFailed
        )
    }
}

/**
 * Load tokens from given token store.
 */
@ExperimentalOpenIdConnect
public fun BearerAuthConfig.loadTokens(tokenStore: TokenStore) {
    loadTokens {
        val accessToken = tokenStore.getAccessToken()
        val refreshToken = tokenStore.getRefreshToken()
        accessToken?.let {
            BearerTokens(
                accessToken = it,
                refreshToken = refreshToken ?: "",
            )
        }
    }
}

/**
 * Refresh tokens using the given refresh callback
 *
 * @param refreshAndSaveTokens The callback receives the old access token and should refresh tokens,
 * _save_ them into e.g. a token store and return them as result.
 *
 * @param onRefreshFailed called when the refresh throws an exception
 */
@ExperimentalOpenIdConnect
public fun BearerAuthConfig.refreshTokens(
    /** receives the old access token **/
    refreshAndSaveTokens: suspend (String) -> OauthTokens?,
    /** called when refresh throws **/
    onRefreshFailed: suspend (Exception) -> Unit
) {
    refreshTokens {
        val newTokens = try {
            refreshAndSaveTokens(this.oldTokens?.accessToken.orEmpty())
        } catch (e: OpenIdConnectException.UnsuccessfulTokenRequest) {
            onRefreshFailed(e)
            null
        } catch (_: OpenIdConnectException) {
            null
        }
        newTokens?.let {
            BearerTokens(
                accessToken = it.accessToken,
                refreshToken = it.refreshToken ?: "",
            )
        }
    }
}
