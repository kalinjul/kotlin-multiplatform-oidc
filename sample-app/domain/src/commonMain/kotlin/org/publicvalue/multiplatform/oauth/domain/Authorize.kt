package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oauth.webserver.Webserver
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

@Inject
class Authorize(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
    private val urlHandler: HandleUrl,
    private val webserver: Webserver
) {
    suspend operator fun invoke(client: Client) {
        logger.d { "Login with $client" }

        withContext(dispatchers.io()) {
            // TODO discover first?
            val idp = idpDao.getIdp(client.idpId).first()
            val client = OpenIDConnectClient {
                endpoints {
                    tokenEndpoint(idp.endpointToken ?: "")
                    authEndpoint(idp.endpointAuthorization ?: "")
                }
                clientId(client.client_id ?: "")
                clientSecret(client.client_secret ?: "")
                scope(client.scope ?: "")
                redirectUri("http://localhost:8080/redirect")
            }

            val request = client.createAuthCodeRequest()

            val code = async {
                urlHandler.invoke(request.url)
                val code = withTimeout(5000) {
                    webserver.startAndWaitForRedirect(Constants.WEBSERVER_PORT)
                }
                code
            }.await()

            println("received code: ${code?.queryParameters?.get("code")}")
            // TODO check state in response
        }
    }
}

