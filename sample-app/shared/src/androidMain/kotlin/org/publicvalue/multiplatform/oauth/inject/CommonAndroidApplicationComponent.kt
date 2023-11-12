package org.publicvalue.multiplatform.oauth.inject

import android.app.Application
import org.publicvalue.multiplatform.oauth.ApplicationInfo
import me.tatarka.inject.annotations.Provides

interface CommonAndroidApplicationComponent : CommonApplicationComponent {

    val application: Application

    @ApplicationScope
    @Provides
    fun provideApplicationInfo(
        application: Application,
    ): ApplicationInfo {
        val packageManager = application.packageManager
        val applicationInfo = packageManager.getApplicationInfo(application.packageName, 0)
        val packageInfo = packageManager.getPackageInfo(application.packageName, 0)

        return ApplicationInfo(
            packageName = application.packageName,
            debugBuild = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            versionName = packageInfo.versionName,
            versionCode = @Suppress("DEPRECATION") packageInfo.versionCode,
        )
    }
}
