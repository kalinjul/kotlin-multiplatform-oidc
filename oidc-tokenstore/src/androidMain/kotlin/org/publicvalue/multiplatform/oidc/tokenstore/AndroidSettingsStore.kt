package org.publicvalue.multiplatform.oidc.tokenstore

import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.get

class AndroidSettingsStore(
    context: Context
) : SettingsStore {

    val settings = SharedPreferencesSettings(context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE))
    override suspend fun get(key: String): String? {
        return settings[key]
    }

    override suspend fun put(key: String, value: String) {
        settings.putString(key, value)
    }

    override suspend fun remove(key: String) {
        settings.remove(key)
    }

    override suspend fun clear() {
        settings.clear()
    }

}