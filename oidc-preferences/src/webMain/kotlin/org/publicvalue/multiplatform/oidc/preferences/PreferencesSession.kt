package org.publicvalue.multiplatform.oidc.preferences

import kotlinx.browser.sessionStorage

class PreferencesSession: Preferences {
    override suspend fun get(key: String): String? {
        return sessionStorage.getItem(key)
    }

    override suspend fun put(key: String, value: String) {
        sessionStorage.setItem(key, value)
    }

    override suspend fun remove(key: String) {
        sessionStorage.removeItem(key)
    }

    /**
     * On WASM, this will clear the whole local storage, which might not be intended.
     */
    override suspend fun clear() {
        sessionStorage.clear()
    }
}