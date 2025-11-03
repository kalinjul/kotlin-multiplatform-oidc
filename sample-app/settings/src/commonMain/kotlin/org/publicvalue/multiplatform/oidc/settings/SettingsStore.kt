package org.publicvalue.multiplatform.oidc.settings

public interface SettingsStore {
    public suspend fun get(key: String): String?
    public suspend fun put(key: String, value: String)
    public suspend fun remove(key: String)
    public suspend fun clear()
}