package org.publicvalue.multiplatform.oidc.sample.data

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import org.publicvalue.multiplatform.oidc.settings.SettingsStore

val LocalSettingsStore: ProvidableCompositionLocal<OidcSettingsStore> = compositionLocalOf { error("Has to be provided") }