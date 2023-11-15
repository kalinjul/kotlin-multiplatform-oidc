package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

@Inject
class Login(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
    private val urlHandler: HandleUrl
) {
    suspend operator fun invoke(client: Client) {
        logger.d { "Login with $client" }

        withContext(dispatchers.io()) {
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

            urlHandler.invoke(request.url)
        }
    }
}

