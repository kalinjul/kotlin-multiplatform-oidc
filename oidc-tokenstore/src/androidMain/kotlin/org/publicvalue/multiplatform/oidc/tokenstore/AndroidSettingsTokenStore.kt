package org.publicvalue.multiplatform.oidc.tokenstore

import android.content.Context
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@ExperimentalOpenIdConnect
@Suppress("unused")
class AndroidSettingsTokenStore(
    context: Context
): SettingsTokenStore(
    settings = AndroidEncryptedPreferencesSettingsStore(context)
)