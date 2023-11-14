package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.ClientDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider

@Inject
class AddClient(
    private val clientDao: ClientDao,
    private val dispatchers: DispatcherProvider
) {
    suspend operator fun invoke(idp: Identityprovider, name: String? = null) {
        withContext(dispatchers.io()) {
            val size = clientDao.getClients().first().size
            clientDao.insert(
                Client(
                    id = 0,
                    idpId = idp.id,
                    name = name ?: "Client ${size+1}",
                    client_id = null,
                    client_secret = null,
                    redirect_uri = null,
                    scope = null,
                    state = null,
                    prompt = null,
                    login_hint = null,
                    domain_hint = null,
                    code_challenge_method = null
                )
            )
        }
    }
}