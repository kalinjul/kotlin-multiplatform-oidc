package org.publicvalue.multiplatform.oidc.okhttp

import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.tokenstore.OauthTokens
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.tokenstore.removeTokens

/**
 * [OpenIdConnectAuthenticator] using [TokenStore] to retrieve tokens and
 * [TokenRefreshHandler] to refresh tokens
 */
@ExperimentalOpenIdConnect
@Suppress("unused")
public open class DefaultOpenIdConnectAuthenticator(
    public open val tokenStore: TokenStore,
    public open val refreshHandler: TokenRefreshHandler,
    public open val client: OpenIdConnectClient
) : OpenIdConnectAuthenticator() {
    override suspend fun getAccessToken(): String? {
        return tokenStore.getAccessToken()
    }

    override suspend fun refreshTokens(oldAccessToken: String): OauthTokens? {
        return refreshHandler.refreshAndSaveToken(client, oldAccessToken)
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
