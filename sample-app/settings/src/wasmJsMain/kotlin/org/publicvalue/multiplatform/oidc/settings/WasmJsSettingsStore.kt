package org.publicvalue.multiplatform.oidc.settings

import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.get
import kotlinx.browser.localStorage
import org.w3c.dom.Storage

class WasmJsSettingsStore: SettingsStore {

    val prefs: StorageSettings

    constructor(storage: Storage) {
        prefs = StorageSettings(storage)
    }

    constructor(): this(localStorage)


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