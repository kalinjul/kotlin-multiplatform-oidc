package org.publicvalue.multiplatform.oidc.preferences

import android.content.Context
import okio.Path.Companion.toPath
import org.publicvalue.multiplatform.oidc.preferences.org.publicvalue.multiplatform.oidc.preferences.PreferencesDataStore

actual class PreferencesFactory actual constructor() {

//    private val store: DataStore<Preferences>? = null
//    /**
//     * Filename must end with ".preferences_pb". It is not possible to create multiple instances like this.
//     */
//    fun getOrCreate(context: Context, filename: String): PreferencesDataStore {
//        println("YY getting preferences for application = ${context.applicationContext}")
//        // get singleton preferencesDataStore
//        val preferencesDataStore = preferencesDataStore(filename).getValue(context.applicationContext, ::store)
//        return PreferencesDataStore(preferencesDataStore)
//    }
//
    /**
     * Filename must end with ".preferences_pb".
     */
    fun create(context: Context, filename: String): PreferencesDataStore {
        return PreferencesDataStore(context.filesDir.resolve(filename).absolutePath.toPath())
    }
}