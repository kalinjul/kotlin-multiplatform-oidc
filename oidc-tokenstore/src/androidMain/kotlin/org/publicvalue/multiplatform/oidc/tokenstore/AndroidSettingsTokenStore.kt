package org.publicvalue.multiplatform.oidc.tokenstore

import android.content.Context
import android.os.Build
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@ExperimentalOpenIdConnect
@Suppress("unused")
class AndroidSettingsTokenStore(
    context: Context
): SettingsTokenStore(
    settings = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        AndroidDataStoreSettingsStore(context)
    } else {
        @Suppress("DEPRECATION")
        AndroidEncryptedPreferencesSettingsStore(context)
    }
)