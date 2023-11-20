package org.publicvalue.multiplatform.oidc.settings

interface SettingsStore {
    suspend fun get(key: String): String?
    suspend fun put(key: String, value: String)
    suspend fun remove(key: String)
    suspend fun clear()
}