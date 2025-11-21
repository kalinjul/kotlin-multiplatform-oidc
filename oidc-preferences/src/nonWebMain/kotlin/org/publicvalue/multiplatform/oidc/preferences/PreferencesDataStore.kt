package org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okio.Path

class PreferencesDataStore(private val dataStore: DataStore<Preferences>): org.publicvalue.multiplatform.oidc.preferences.Preferences {

    constructor(preferencesPath: Path) : this(
        PreferenceDataStoreFactory.createWithPath(
            migrations = emptyList(),
            corruptionHandler = null,
            scope = CoroutineScope(context = Dispatchers.IO + SupervisorJob()),
            produceFile = { preferencesPath }
        )
    )

    override suspend fun get(key: String): String? {
        val prefKey = stringPreferencesKey(key)
        return dataStore.data
            .map { prefs -> prefs[prefKey] }
            .first()
    }

    override suspend fun put(key: String, value: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { prefs ->
            prefs[prefKey] = value
        }
    }

    override suspend fun remove(key: String) {
        val prefKey = stringPreferencesKey(key)
        dataStore.edit { prefs ->
            prefs.remove(prefKey)
        }
    }

    override suspend fun clear() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}