package org.publicvalue.multiplatform.oauth.data.daos

import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.Database
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope

@Inject
@ApplicationScope
class IdpDao(override val db: Database) : SqlDelightDao<Identityprovider> {
    override fun insert(entity: Identityprovider): Long {
        TODO("Not yet implemented")
    }

    override fun update(entity: Identityprovider) {
        TODO("Not yet implemented")
    }

    override fun deleteEntity(entity: Identityprovider) {
        TODO("Not yet implemented")
    }

    override fun getExistingIds(ids: List<Long>): List<Long> {
        TODO("Not yet implemented")
    }
}