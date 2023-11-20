package org.publicvalue.multiplatform.oauth.settings

import me.tatarka.inject.annotations.Inject

@Inject
class JvmSettingsStore : SettingsStore {

    override suspend fun get(key: String): String? {
        return null
    }

    override suspend fun put(key: String, value: String) {
    }

    override suspend fun remove(key: String) {
    }

    override suspend fun clear() {
    }
}