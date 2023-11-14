package org.publicvalue.multiplatform.oauth.domain

import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider

@Inject
class Login(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger
) {
    suspend operator fun invoke(client: Client) {
        logger.d { "Login with $client" }

//        withContext(dispatchers.io()) {
//            // set en   dpoints to null first for ui observers to trigger
//            idpDao.update(idp.copy(
//                endpointToken = null,
//                endpointAuthorization = null,
//                endpointEndSession = null,
//                endpointUserInfo = null,
//                endpointIntrospection = null,
//                endpointDeviceAuthorization = null
//            ))
//
//            idp.discoveryUrl?.let {
//                val config = Discover().downloadConfiguration(it)
//                idpDao.update(
//                    idp.updateWith(config)
//                )
//            }
//
//        }
    }
}

