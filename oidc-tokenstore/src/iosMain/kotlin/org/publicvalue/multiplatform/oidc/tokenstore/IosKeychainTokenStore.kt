package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import kotlin.experimental.ExperimentalObjCName

@ExperimentalOpenIdConnect
@OptIn(ExperimentalObjCName::class)
@ObjCName("KeychainTokenStore", "KeychainTokenStore", exact = true)
@Suppress("unused")
/**
 * Uses the keychain to save and retrieve tokens.
 */
class IosKeychainTokenStore: SettingsTokenStore(settings = IosKeychainSettingsStore())