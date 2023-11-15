package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider

@Inject
class ExchangeToken(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) {
    suspend operator fun invoke(client: Client, code: String) {
        logger.d { "Exchange token with $client, authCode: $code" }

        withContext(dispatchers.io()) {

        }
    }
}

