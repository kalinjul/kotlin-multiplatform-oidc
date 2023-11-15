package org.publicvalue.multiplatform.oauth.data.inject

import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.data.Database
import org.publicvalue.multiplatform.oauth.data.DatabaseConfiguration
import org.publicvalue.multiplatform.oauth.data.DestructiveMigrationSchema
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope

expect interface SqlDelightDatabasePlatformComponent

interface SqlDelightDatabaseComponent : SqlDelightDatabasePlatformComponent {
    @ApplicationScope
    @Provides
    fun provideSqlDelightDatabase(
        driver: SqlDriver,
    ): Database {
        // TODO remove
//        DestructiveMigrationSchema.migrate(
//            driver,
//            0L,
//            0L
//        )

        return Database(
            driver = driver,
            clientAdapter = Client.Adapter(
                code_challenge_methodAdapter = EnumColumnAdapter()
            )
        )
    }

    @Provides
    fun provideDatabaseConfiguration(): DatabaseConfiguration = DatabaseConfiguration(inMemory = false)
}
