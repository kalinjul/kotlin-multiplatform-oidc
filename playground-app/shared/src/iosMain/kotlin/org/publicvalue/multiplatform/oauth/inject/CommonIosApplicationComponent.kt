package org.publicvalue.multiplatform.oauth.inject

import androidx.compose.ui.unit.Density
import org.publicvalue.multiplatform.oauth.ApplicationInfo
import me.tatarka.inject.annotations.Provides
import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIScreen
import kotlin.experimental.ExperimentalNativeApi


interface CommonIosApplicationComponent : CommonApplicationComponent {

    @Provides
    fun provideNsUserDefaults(): NSUserDefaults = NSUserDefaults.standardUserDefaults

    @Provides
    fun provideDensity(): Density = Density(density = UIScreen.mainScreen.scale.toFloat())

    @OptIn(ExperimentalNativeApi::class)
    @ApplicationScope
    @Provides
    fun provideApplicationInfo(): ApplicationInfo = ApplicationInfo(
        packageName = NSBundle.mainBundle.bundleIdentifier ?: error("Bundle ID not found"),
        debugBuild = Platform.isDebugBinary,
        versionName = NSBundle.mainBundle.infoDictionary
            ?.get("CFBundleShortVersionString") as? String
            ?: "",
        versionCode = (NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String)
            ?.toIntOrNull()
            ?: 0,
    )
}
