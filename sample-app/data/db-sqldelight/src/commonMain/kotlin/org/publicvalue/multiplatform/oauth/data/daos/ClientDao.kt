package org.publicvalue.multiplatform.oauth.data.daos

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.Database
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider

@Inject
@ApplicationScope
class ClientDao(
    override val db: Database,
    private val dispatchers: DispatcherProvider,
) : SqlDelightDao<Client> {
    override fun insert(entity: Client): Long {
        db.clientsQueries.insert(
            id = entity.id,
            idpId = entity.idpId,
            name = entity.name,
            client_id = entity.client_id,
            client_secret = entity.client_secret,
            redirect_uri = entity.redirect_uri,
            scope = entity.scope,
            state = entity.state,
            prompt = entity.prompt,
            login_hint = entity.login_hint,
            domain_hint = entity.domain_hint,
            code_challenge_method = entity.code_challenge_method
        )
        return db.idpsQueries.lastInsertRowId().executeAsOne()
    }

    override fun update(entity: Client) {
        db.clientsQueries.update(
            id = entity.id,
            idpId = entity.idpId,
            name = entity.name,
            client_id = entity.client_id,
            client_secret = entity.client_secret,
            redirect_uri = entity.redirect_uri,
            scope = entity.scope,
            state = entity.state,
            prompt = entity.prompt,
            login_hint = entity.login_hint,
            domain_hint = entity.domain_hint,
            code_challenge_method = entity.code_challenge_method
        )
    }

    override fun deleteEntity(entity: Client) {
        db.clientsQueries.delete(entity.id)
    }

    fun getClients(): Flow<List<Client>> {
        return db.clientsQueries.getAll().asFlow()
            .mapToList(dispatchers.io())
    }

    fun getClient(clientId: Long): Flow<Client> {
        return db.clientsQueries.get(clientId)
            .asFlow()
            .mapToOne(dispatchers.io())
    }
}