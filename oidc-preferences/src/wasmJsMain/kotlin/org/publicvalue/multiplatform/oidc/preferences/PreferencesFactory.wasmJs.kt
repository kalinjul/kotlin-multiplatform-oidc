package org.publicvalue.multiplatform.oidc.preferences

actual class PreferencesFactory {
    fun create(): Preferences {
        return PreferencesSession()
    }
}