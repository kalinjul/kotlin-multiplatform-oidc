package org.publicvalue.multiplatform.oauth.inject

import me.tatarka.inject.annotations.Inject
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface NoOpAppInitializerComponent {
    @Provides
    @IntoSet
    fun provideNoOpAppInitializer(impl: NoOpAppInitializer): AppInitializer = impl
}

@Inject
class NoOpAppInitializer(
): AppInitializer {
    override fun initialize() {
    }
}