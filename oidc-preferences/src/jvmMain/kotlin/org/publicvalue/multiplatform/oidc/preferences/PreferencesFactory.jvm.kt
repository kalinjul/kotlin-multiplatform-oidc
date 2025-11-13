package org.publicvalue.multiplatform.oidc.preferences

import okio.Path.Companion.toPath
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore
import java.io.File

actual class PreferencesFactory actual constructor() {
    /**
     * Filename must end with ".preferences_pb"
     */
    fun create(filename: String): PreferencesDataStore {
        val home = System.getProperty("user.home")
        val path = File(home, "beihilfeapp/$filename").absolutePath.toPath()
        return PreferencesDataStore(path)
    }
}