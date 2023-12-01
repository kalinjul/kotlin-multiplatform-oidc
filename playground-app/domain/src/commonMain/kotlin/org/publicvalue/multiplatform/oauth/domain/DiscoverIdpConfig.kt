package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oidc.discovery.OpenIDConnectDiscover

@Inject
class DiscoverIdpConfig(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger
) {
    suspend operator fun invoke(idp: Identityprovider) {
        logger.d { "Discovering with $idp" }

        withContext(dispatchers.io()) {
            // set en   dpoints to null first for ui observers to trigger
            idpDao.update(idp.copy(
                endpointToken = null,
                endpointAuthorization = null,
                endpointEndSession = null,
                endpointUserInfo = null,
                endpointIntrospection = null,
                endpointDeviceAuthorization = null
            ))

            idp.discoveryUrl?.let {
                val config = OpenIDConnectDiscover().downloadConfiguration(it)
                idpDao.update(
                    idp.updateWith(config)
                )
            }

//            idpDao.update(idp.copy(endpointToken = "Discovered!"))
        }
    }
}

