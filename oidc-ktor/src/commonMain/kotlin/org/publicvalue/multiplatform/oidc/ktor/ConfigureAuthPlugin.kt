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
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse

/**
 * Configure Bearer Authentication using TokenStore + RefreshHandler.
 *
 * Tokens are removed if a refresh fails with a non-temporary exception (e.g. invalid_grant on token expiry)
 */
@ExperimentalOpenIdConnect
fun AuthConfig.oidcBearer(
    tokenStore: TokenStore,
    refreshHandler: TokenRefreshHandler,
    client: OpenIdConnectClient,
    onRefreshFailed: suspend (Exception) -> Unit = {
        if ((it is OpenIdConnectException.UnsuccessfulTokenRequest && it.errorResponse?.error in ErrorResponse.Error.tokenErrors) || it is OpenIdConnectException.TokenExpired) {
            // if we have real, permanent error on token refresh or the token is expired, remove tokens.
            tokenStore.removeTokens()
        }
    }
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
 * @param refreshAndSaveTokens Callback that is used to refresh the token.
 * Receives the old token for comparison and should save new tokens.
 *
 * @param onRefreshFailed called when refresh call fails
 */
@ExperimentalOpenIdConnect
fun AuthConfig.oidcBearer(
    tokenStore: TokenStore,
    refreshAndSaveTokens: suspend (String) -> OauthTokens?,
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
fun BearerAuthConfig.loadTokens(tokenStore: TokenStore) {
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
 * @param onRefreshFailed called when refresh call fails
 */
@ExperimentalOpenIdConnect
fun BearerAuthConfig.refreshTokens(
    refreshAndSaveTokens: suspend (String) -> OauthTokens?,
    onRefreshFailed: suspend (OpenIdConnectException) -> Unit,
) {
    refreshTokens {
        val newTokens = try {
            refreshAndSaveTokens(this.oldTokens?.accessToken.orEmpty())
        } catch (e: OpenIdConnectException.UnsuccessfulTokenRequest) {
            onRefreshFailed(e)
            throw e
        } catch (e: OpenIdConnectException.TokenExpired) {
            onRefreshFailed(e)
            throw e
        } catch (e: Exception) {
            throw e
        }
        newTokens?.let {
            BearerTokens(
                accessToken = it.accessToken,
                refreshToken = it.refreshToken ?: "",
            )
        }
    }
}
