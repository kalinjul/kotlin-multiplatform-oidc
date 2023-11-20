package org.publicvalue.multiplatform.oauth.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.Database
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider

@Inject
@ApplicationScope
class IdpDao(
    override val db: Database,
    private val dispatchers: DispatcherProvider,
) : SqlDelightDao<Identityprovider> {
    override fun insert(entity: Identityprovider): Long {
        db.idpsQueries.insert(
            id = entity.id,
            name = entity.name,
            useDiscovery = entity.useDiscovery,
            discoveryUrl = entity.discoveryUrl,
            endpointAuthorization = entity.endpointAuthorization,
            endpointDeviceAuthorization = entity.endpointDeviceAuthorization,
            endpointToken = entity.endpointToken,
            endpointEndSession = entity.endpointEndSession,
            endpointIntrospection = entity.endpointIntrospection,
            endpointUserInfo = entity.endpointUserInfo
        )
        return db.idpsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: Identityprovider) {
        db.idpsQueries.update(
            id = entity.id,
            name = entity.name,
            useDiscovery = entity.useDiscovery,
            discoveryUrl = entity.discoveryUrl,
            endpointAuthorization = entity.endpointAuthorization,
            endpointDeviceAuthorization = entity.endpointDeviceAuthorization,
            endpointToken = entity.endpointToken,
            endpointEndSession = entity.endpointEndSession,
            endpointIntrospection = entity.endpointIntrospection,
            endpointUserInfo = entity.endpointUserInfo
        )
    }

    override fun deleteEntity(entity: Identityprovider) {
        db.idpsQueries.delete(entity.id)
    }

    fun getIdps(): Flow<List<Identityprovider>> {
        return db.idpsQueries.getAll().asFlow()
            .mapToList(dispatchers.io())
    }

    fun getIdp(idpId: Long): Flow<Identityprovider> {
        return db.idpsQueries.get(idpId)
            .asFlow()
            .mapToOne(dispatchers.io())
    }
}