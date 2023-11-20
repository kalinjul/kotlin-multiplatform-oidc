package org.publicvalue.multiplatform.oauth.settings

import kotlinx.serialization.json.Json

interface SettingsStore {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}