package org.publicvalue.multiplatform.oidc.preferences

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.jvm.JvmStatic

abstract class PreferencesSingletonFactory {
    companion object {
        @JvmStatic
        protected val mutex = Mutex()

        @JvmStatic
        @kotlin.concurrent.Volatile
        protected var INSTANCE: Preferences? = null
    }

    suspend fun getOrCreate(filename: String): Preferences {
        return INSTANCE ?: mutex.withLock {
            if (INSTANCE == null) {
                create(filename).also {
                    INSTANCE = it
                }
            } else INSTANCE!!
        }
    }

    abstract fun create(filename: String): Preferences
}

expect class PreferencesFactory