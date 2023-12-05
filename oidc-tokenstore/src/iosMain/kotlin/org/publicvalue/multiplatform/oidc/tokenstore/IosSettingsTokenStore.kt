package org.publicvalue.multiplatform.oidc.tokenstore

import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("SettingsTokenStore", "SettingsTokenStore", exact = true)
@Suppress("unused")
class IosSettingsTokenStore: AbstractSettingsTokenStore(settings = IosSettingsStore())