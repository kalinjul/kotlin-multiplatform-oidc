package org.publicvalue.multiplatform.oidc.preferences

import android.content.Context
import okio.Path.Companion.toPath
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore

actual class PreferencesFactory(context: Context) : PreferencesSingletonFactory() {

    private val filesDir = context.filesDir

    override fun create(filename: String): Preferences {
        return PreferencesDataStore(filesDir.resolve(filename).absolutePath.toPath())
    }
}