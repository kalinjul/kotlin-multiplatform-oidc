package org.publicvalue.multiplatform.oidc.tokenstore

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
class InMemorySettingsStore : SettingsStore {

    private val memory = hashMapOf<String, String>()

    override suspend fun get(key: String): String? = memory[key]

    override suspend fun put(key: String, value: String) {
        memory[key] = value
    }

    override suspend fun remove(key: String) {
        memory.remove(key)
    }

    override suspend fun clear() {
        memory.clear()
    }
}
