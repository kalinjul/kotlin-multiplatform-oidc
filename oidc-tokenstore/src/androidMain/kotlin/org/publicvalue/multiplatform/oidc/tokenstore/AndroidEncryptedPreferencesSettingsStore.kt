package org.publicvalue.multiplatform.oidc.tokenstore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.get

class AndroidEncryptedPreferencesSettingsStore(
    context: Context
) : SettingsStore {

    var masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    var sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "${context.packageName}.auth",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val settings = SharedPreferencesSettings(
        sharedPreferences
    )
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