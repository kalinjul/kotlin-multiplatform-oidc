package org.publicvalue.multiplatform.oidc.tokenstore

import android.content.Context

@Suppress("unused")
class AndroidSettingsTokenStore(
    context: Context
): AbstractSettingsTokenStore(
    settings = AndroidSettingsStore(context)
)