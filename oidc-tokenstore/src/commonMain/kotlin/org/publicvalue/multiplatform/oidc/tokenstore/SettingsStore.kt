package org.publicvalue.multiplatform.oidc.tokenstore

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
public interface SettingsStore {
    public suspend fun get(key: String): String?
    public suspend fun put(key: String, value: String)
    public suspend fun remove(key: String)
    public suspend fun clear()
}
