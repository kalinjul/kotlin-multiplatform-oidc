import android.os.Bundle
import androidx.activity.ComponentActivity
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.AndroidAuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.TokenStore
import org.publicvalue.multiplatform.oidc.appsupport.AuthFlowFactory
import org.publicvalue.multiplatform.oidc.tokenstore.TokenRefreshHandler
import org.publicvalue.multiplatform.oidc.tokenstore.saveTokens
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.parseJwt
import org.publicvalue.multiplatform.oidc.types.remote.AuthResult
import kotlin.experimental.ExperimentalObjCRefinement


/**
 * Sample code for Readme
 */
@OptIn(ExperimentalObjCRefinement::class, ExperimentalOpenIdConnect::class)
object README {
    val client = OpenIdConnectClient {  }
    val authFlowFactory: AuthFlowFactory = TODO()
    val tokens: AuthResult.AccessToken = TODO()
    val idToken: String = TODO()
    val tokenstore: TokenStore = TODO()
    val token: String = TODO()
    val refreshHandler: TokenRefreshHandler = TODO()

    // Create OpenID config and client
    fun `Create_OpenID_config_and_client`() {
        val client = OpenIdConnectClient(discoveryUri = "<discovery url>") {
            endpoints {
                tokenEndpoint = "<tokenEndpoint>"
                authorizationEndpoint = "<authorizationEndpoint>"
                userInfoEndpoint = null
                endSessionEndpoint = "<endSessionEndpoint>"
            }

            clientId = "<clientId>"
            clientSecret = "<clientSecret>"
            scope = "openid profile"
            codeChallengeMethod = CodeChallengeMethod.S256
            redirectUri = "<redirectUri>"
        }
    }

    // Create AuthFlowFactory in onCreate
    fun `Create_AuthFlowFactory_in_onCreate`() {
        class MainActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                val factory = AndroidAuthFlowFactory()
                factory.registerActivity(this)
            }
        }
    }

    // Request access token using code auth flow
    suspend fun `Request_access_token_using_code_auth_flow`() {
        val flow = authFlowFactory.createAuthFlow(client)
        val tokens = flow.getAccessToken()
    }

    // perform refresh or endSession
    suspend fun `perform_refresh_or_endSession`() {
        tokens.refresh_token?.let { client.refreshToken(refreshToken = it) }
        tokens.id_token?.let { client.endSession(idToken = it) }
    }

    // Custom headers/url parameters
    suspend fun `Custom_headers_url_parameters`() {
        client.endSession(idToken = idToken) {
            headers.append("X-CUSTOM-HEADER", "value")
            url.parameters.append("custom_parameter", "value")
        }
    }

    // We provide simple JWT parsing
    fun `We_provide_simple_JWT_parsing`() {
        val tokens = AuthResult.AccessToken("abc")
        val jwt = tokens.id_token?.parseJwt()
        println(jwt?.payload?.aud) // print audience
        println(jwt?.payload?.iss) // print issuer
        println(jwt?.payload?.additionalClaims?.get("email")) // get claim
    }


    // token store
    @OptIn(ExperimentalOpenIdConnect::class)
    suspend fun `tokenstore`() {
        tokenstore.saveTokens(tokens)
        val accessToken = tokenstore.getAccessToken()
    }

    // refresh handler
    @OptIn(ExperimentalOpenIdConnect::class)
    suspend fun `refresh_handler`() {
        val refreshHandler = TokenRefreshHandler(tokenStore = tokenstore)
        refreshHandler.refreshAndSaveToken(client, oldAccessToken = token) // thread-safe refresh and save new tokens to store
    }
}
