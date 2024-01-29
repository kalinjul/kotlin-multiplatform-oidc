package org.publicvalue.multiplatform.oidc.ktor

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

@OptIn(ExperimentalOpenIdConnect::class)
object README {

    val tokenStore: TokenStore = null!!
    val refreshHandler: TokenRefreshHandler = null!!
    val client: OpenIdConnectClient = null!!
    val engine: HttpClientEngine = null!!

    fun `configureKtorAuth`() {
        HttpClient(engine) {
            install(Auth) {
                oidcBearer(
                    tokenStore = tokenStore,
                    refreshHandler = refreshHandler,
                    client = client,
                )
            }
        }
    }

}