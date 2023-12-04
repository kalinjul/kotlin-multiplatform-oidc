import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.appsupport.AuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.Jwt
import org.publicvalue.multiplatform.oidc.types.parseJwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC


/**
 * Sample code for Readme
 */
@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
object Readme {
    val client = OpenIdConnectClient {  }
    val authFlowFactory: AuthFlowFactory = TODO()
    val tokens: AccessTokenResponse = TODO()

    fun `Create OpenID config and client`() {
        val config = OpenIdConnectClient(discoveryUri = "<discovery url>") {
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

    fun `Create AuthFlowFactory in onCreate`() {
//        class MainActivity : ComponentActivity() {
//            override fun onCreate(savedInstanceState: Bundle?) {
//                super.onCreate(savedInstanceState)
//                val factory = AndroidAuthFlowFactory(this)
//            }
//        }
    }

    suspend fun `Request access token using code auth flow`() {
        val flow = authFlowFactory.createAuthFlow(client)
        val tokens = flow.getAccessToken()
    }

    suspend fun `perform refresh or endSession`() {
        tokens.refresh_token?.let { client.refreshToken(refreshToken = it) }
        tokens.id_token?.let { client.endSession(idToken = it) }
    }


    fun documentation() {
        val tokens = AccessTokenResponse("abc")
        val jwt = tokens.id_token?.parseJwt()
        println(jwt?.payload?.aud) // print audience
        println(jwt?.payload?.iss) // print issuer
        println(jwt?.payload?.additionalClaims?.get("email")) // get claim
    }
}
