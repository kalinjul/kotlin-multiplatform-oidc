package org.publicvalue.multiplatform.oauth.data.inject

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.data.Database
import org.publicvalue.multiplatform.oauth.data.DatabaseConfiguration
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import java.io.File

actual interface SqlDelightDatabasePlatformComponent {
    @Provides
    @ApplicationScope
    fun provideDriverFactory(
        configuration: DatabaseConfiguration,
    ): SqlDriver = JdbcSqliteDriver(
        url = when {
            configuration.inMemory -> JdbcSqliteDriver.IN_MEMORY
            else -> "jdbc:sqlite:${getDatabaseFile().absolutePath}"
        },
    ).also { db ->
        Database.Schema.create(db)
        db.execute(null, "PRAGMA foreign_keys=ON", 0)
    }
}

private fun getDatabaseFile(): File {
    return File(
        appDir.also { if (!it.exists()) it.mkdirs() },
        "oidcplayground.db",
    )
}

private val appDir: File
    get() {
        val os = System.getProperty("os.name").lowercase()
        return when {
            os.contains("win") -> {
                File(System.getenv("AppData"), "oidcplayground/db")
            }

            os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                File(System.getProperty("user.home"), ".oidcplayground")
            }

            os.contains("mac") -> {
                File(System.getProperty("user.home"), "Library/Application Support/oidcplayground")
            }

            else -> error("Unsupported operating system")
        }
    }
