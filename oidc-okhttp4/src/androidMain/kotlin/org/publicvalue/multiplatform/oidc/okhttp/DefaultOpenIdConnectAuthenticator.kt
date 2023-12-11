package org.publicvalue.multiplatform.oidc.okhttp

import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.removeTokens

/**
 * [OpenIdConnectAuthenticator] using [TokenStore] to retrieve tokens and
 * [TokenRefreshHandler] to refresh tokens
 */
@ExperimentalOpenIdConnect
@Suppress("unused")
open class DefaultOpenIdConnectAuthenticator(
    val tokenStore: TokenStore,
    val refreshHandler: TokenRefreshHandler,
    val client: OpenIdConnectClient
): OpenIdConnectAuthenticator() {
    override suspend fun getAccessToken(): String? {
        return tokenStore.getAccessToken()
    }

    override suspend fun refreshTokens(oldAccessToken: String) {
        refreshHandler.safeRefreshToken(client, oldAccessToken)
    }

    override fun onRefreshFailed() {
        runBlocking {
            tokenStore.removeTokens()
        }
    }

    override fun buildRequest(builder: Request.Builder) {
        super.buildRequest(builder)
    }
}