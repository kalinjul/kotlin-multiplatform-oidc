package org.publicvalue.multiplatform.oauth.data.daos

import org.publicvalue.multiplatform.oauth.data.Database

interface SqlDelightDao<in E> : BaseDao<E> {
    val db: Database

    override fun insert(entities: List<E>) {
        db.transaction {
            for (entity in entities) {
                insert(entity)
            }
        }
    }
}


interface BaseDao<in E> {
    fun insert(entity: E): Long
    fun insert(entities: List<E>)

    fun update(entity: E)
//    fun <ET : IdentifiableEntity> insertOrReplace(
//        entity: ET,
//        insert: (ET) -> Long,
//        update: (ET) -> Unit,
//    ): Long {
//        return if (entity.id != 0L) {
//            update(entity)
//            entity.id
//        } else {
//            insert(entity)
//        }
//    }

    fun deleteEntity(entity: E)

    fun getExistingIds(ids: List<Long>): List<Long>
}