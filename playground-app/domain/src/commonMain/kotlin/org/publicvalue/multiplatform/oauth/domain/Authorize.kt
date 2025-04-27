package org.publicvalue.multiplatform.oauth.domain

import kotlinx.coroutines.flow.Flow
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.domain.types.AuthorizeResult

expect class Authorize {
    suspend operator fun invoke(client: Client): Flow<AuthorizeResult>
}