package org.publicvalue.multiplatform.oidc.appsupport.customtab

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.core.net.toUri

private val ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService"

fun Context.getCustomTabProviders(): List<ResolveInfo> {
    val activityIntent = Intent(Intent.ACTION_VIEW, "http://www.example.com".toUri())

    // Get all apps that can handle VIEW intents.
    val resolvedActivityList = packageManager.queryIntentActivities(activityIntent, PackageManager.MATCH_ALL)
    val serviceIntent = Intent()
    serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION)
    val customTabProviders = resolvedActivityList.filter {
        serviceIntent.setPackage(it.activityInfo.packageName)
        packageManager.resolveService(serviceIntent, 0) != null
    }
    return customTabProviders
}