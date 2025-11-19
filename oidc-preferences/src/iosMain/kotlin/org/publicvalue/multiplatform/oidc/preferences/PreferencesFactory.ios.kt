package org.publicvalue.multiplatform.oidc.preferences

import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class PreferencesFactory: PreferencesSingletonFactory() {
    @OptIn(ExperimentalForeignApi::class)
    override fun create(filename: String): Preferences {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )

        val path = requireNotNull(value = documentDirectory).path + "/$filename"

        return PreferencesDataStore(path.toPath())
    }
}