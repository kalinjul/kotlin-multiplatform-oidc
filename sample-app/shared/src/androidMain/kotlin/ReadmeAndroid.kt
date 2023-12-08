import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.okhttp.OpenIdConnectAuthenticator
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore

@OptIn(ExperimentalOpenIdConnect::class)
object ReadmeAndroid {

    val client = OpenIdConnectClient {  }
    val tokenStore: TokenStore = TODO()
    val token: String = TODO()
    val refreshHandler: TokenRefreshHandler = TODO()

    // okhttp
    suspend fun `okhttp`() {
        val authenticator = OpenIdConnectAuthenticator {
            getAccessToken { tokenStore.getAccessToken() }
            refreshTokens { oldAccessToken -> refreshHandler.safeRefreshToken(client, oldAccessToken) }
            onRefreshFailed {
                // provided by app: user has to authenticate again
            }
            buildRequest {
                header("AdditionalHeader", "value") // add custom header to all requests
            }
        }

//        val okHttpClient = OkHttpClient.Builder()
//            .authenticator(authenticator)
//            .build()
    }
}
