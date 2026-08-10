package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import okio.buffer
import okio.source
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME
import java.io.IOException

private const val PREFERENCES_FILE_SUFFIX = ".preferences_pb"
private val oidcSessionDataStoreName = PREFERENCES_FILENAME.removeSuffix(PREFERENCES_FILE_SUFFIX)
private val legacyOidcSessionDataStoreName = "$oidcSessionDataStoreName."

internal val Context.dataStore by preferencesDataStore(
    name = oidcSessionDataStoreName,
    produceMigrations = { context ->
        listOf(LegacyOidcSessionDataStoreMigration(context))
    }
)

/**
 * Migrate data from org.publicvalue.multiplatform.oidc.oidcsession..preferences_pb to
 * the correct org.publicvalue.multiplatform.oidc.oidcsession.preferences_pb
 */
private class LegacyOidcSessionDataStoreMigration(context: Context) : DataMigration<Preferences> {
    private val legacyFile = context.preferencesDataStoreFile(legacyOidcSessionDataStoreName)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean {
        return legacyFile.exists()
    }

    override suspend fun migrate(currentData: Preferences): Preferences {
        return if (currentData.asMap().isNotEmpty()) {
            currentData
        } else {
            readLegacyPreferences() ?: currentData
        }
    }

    override suspend fun cleanUp() {
        if (legacyFile.exists() && !legacyFile.delete()) {
            throw IOException("Could not delete legacy OIDC session DataStore file: $legacyFile")
        }
    }

    private suspend fun readLegacyPreferences(): Preferences? {
        return try {
            legacyFile.source().buffer().use { source ->
                PreferencesSerializer.readFrom(source)
            }
        } catch (_: IOException) {
            null
        }
    }
}
