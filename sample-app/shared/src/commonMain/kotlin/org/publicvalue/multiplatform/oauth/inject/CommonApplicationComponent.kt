package org.publicvalue.multiplatform.oauth.inject

import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oauth.util.DispatcherProviderImpl
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.logging.StdoutLogger
import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.data.inject.SqlDelightDatabaseComponent
import org.publicvalue.multiplatform.oauth.settings.SettingsComponent

interface CommonApplicationComponent:
    SqlDelightDatabaseComponent,
    SettingsComponent,
    NoOpAppInitializerComponent
{
    val initializers: Set<AppInitializer>

    @ApplicationScope
    @Provides
    fun provideCoroutineDispatchers(impl: DispatcherProviderImpl): DispatcherProvider = impl

    @Provides
    fun provideLogger(impl: StdoutLogger): Logger = impl
}