package org.publicvalue.multiplatform.oidc.okhttp

import okhttp3.OkHttpClient
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

// ok http readme section
@OptIn(ExperimentalOpenIdConnect::class)
@Suppress("unused")
object Readme {
    val tokenStore: TokenStore = null!!
    val refreshHandler: TokenRefreshHandler = null!!
    val client: OpenIdConnectClient = null!!

    fun configureOkHttpAuth() {
        @OptIn(ExperimentalOpenIdConnect::class)
        val authenticator = OpenIdConnectAuthenticator {
            getAccessToken { tokenStore.getAccessToken() }
            refreshTokens { oldAccessToken -> refreshHandler.refreshAndSaveToken(client, oldAccessToken) }
            onRefreshFailed {
                // provided by app: user has to authenticate again
            }
            buildRequest {
                header("AdditionalHeader", "value") // add custom header to all requests
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .authenticator(authenticator)
            .build()
    }
}
