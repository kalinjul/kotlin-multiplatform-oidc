package org.publicvalue.multiplatform.oauth.settings

import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope

actual interface SettingsComponent {
    @ApplicationScope
    @Provides
    fun provideSettings(store: JvmSettingsStore): SettingsStore = store

}
