package org.publicvalue.multiplatform.oidc.preferences

import android.content.Context
import okio.Path.Companion.toPath
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore

actual class PreferencesFactory actual constructor() {

    /**
     * Filename must end with ".preferences_pb".
     */
    fun create(context: Context, filename: String): PreferencesDataStore {
        return PreferencesDataStore(context.filesDir.resolve(filename).absolutePath.toPath())
    }
}