package org.publicvalue.multiplatform.oidc.settings

import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.get

class WebMainSettingsStore : SettingsStore {

    private val prefs: StorageSettings = StorageSettings()

    override suspend fun get(key: String): String? {
        return prefs[key]
    }

    override suspend fun put(key: String, value: String) {
        prefs.putString(key, value)
    }

    override suspend fun remove(key: String) {
        prefs.remove(key)
    }

    override suspend fun clear() {
        prefs.clear()
    }

}