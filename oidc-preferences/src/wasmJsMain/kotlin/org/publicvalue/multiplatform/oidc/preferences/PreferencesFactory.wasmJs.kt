package org.publicvalue.multiplatform.oidc.preferences

actual class PreferencesFactory actual constructor() {
    fun create(): Preferences {
        return PreferencesSession()
    }
}