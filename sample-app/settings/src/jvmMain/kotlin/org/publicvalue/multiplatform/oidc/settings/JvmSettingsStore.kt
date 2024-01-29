package org.publicvalue.multiplatform.oidc.settings

import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.get
import java.util.prefs.Preferences

class JvmSettingsStore: SettingsStore {

    val prefs = PreferencesSettings(Preferences.userRoot())

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