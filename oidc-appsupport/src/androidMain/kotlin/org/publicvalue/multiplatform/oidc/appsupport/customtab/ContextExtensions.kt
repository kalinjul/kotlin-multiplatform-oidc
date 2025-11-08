package org.publicvalue.multiplatform.oidc.appsupport.customtab

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.core.net.toUri

private const val ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService"

internal fun Context.getCustomTabProviders(): List<ResolveInfo> {
    val activityIntent = Intent(Intent.ACTION_VIEW, "http://www.example.com".toUri())

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        packageManager.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
    } else {
        packageManager.queryIntentActivities(activityIntent, 0)
    }
    val serviceIntent = Intent()
    serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION)
    val customTabProviders = resolvedActivityList.filter {
        serviceIntent.setPackage(it.activityInfo.packageName)
        packageManager.resolveService(serviceIntent, 0) != null
    }
    return customTabProviders
}
