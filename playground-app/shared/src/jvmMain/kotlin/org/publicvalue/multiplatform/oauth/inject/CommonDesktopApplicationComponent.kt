package org.publicvalue.multiplatform.oauth.inject

import org.publicvalue.multiplatform.oauth.ApplicationInfo
import me.tatarka.inject.annotations.Provides

interface CommonDesktopApplicationComponent : CommonApplicationComponent {

    @ApplicationScope
    @Provides
    fun provideApplicationInfo(
    ): ApplicationInfo = ApplicationInfo(
        packageName = "app.tivi",
        debugBuild = true,
        versionName = "1.0.0",
        versionCode = 1,
    )
    
    companion object
}
