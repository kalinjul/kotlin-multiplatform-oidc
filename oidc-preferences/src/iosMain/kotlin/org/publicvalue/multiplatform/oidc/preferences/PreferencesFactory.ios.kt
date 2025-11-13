package org.publicvalue.multiplatform.oidc.preferences

import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual class PreferencesFactory actual constructor() {
    /**
     * Filename must end with ".preferences_pb"
     */
    @OptIn(ExperimentalForeignApi::class)
    fun create(filename: String): PreferencesDataStore {
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