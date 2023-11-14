package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider

@Inject
class AddIdp(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(name: String? = null) {
        withContext(dispatchers.io()) {
            val size = idpDao.getIdps().first().size
            idpDao.insert(
                Identityprovider(
                    id = 0,
                    name = name ?: "Idp ${size+1}",
                    useDiscovery = true,
                    discoveryUrl = null,
                    endpointToken = null,
                    endpointAuthorization = null,
                    endpointDeviceAuthorization = null,
                    endpointEndSession = null,
                    endpointUserInfo = null,
                    endpointIntrospection = null
                )
            )
        }
    }
}