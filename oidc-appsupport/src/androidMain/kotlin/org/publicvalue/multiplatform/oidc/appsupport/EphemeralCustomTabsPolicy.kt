package org.publicvalue.multiplatform.oidc.appsupport

internal enum class EphemeralCustomTabsLaunchMode {
    EphemeralCustomTab,
    NormalCustomTab,
    PrivateWebView,
}

internal fun resolveEphemeralCustomTabsLaunchMode(
    ephemeralSession: Boolean,
    ephemeralBrowsingSupported: Boolean,
    unsupportedEphemeralCustomTabsFallback: UnsupportedEphemeralCustomTabsFallback,
): EphemeralCustomTabsLaunchMode {
    if (!ephemeralSession) {
        return EphemeralCustomTabsLaunchMode.NormalCustomTab
    }

    if (ephemeralBrowsingSupported) {
        return EphemeralCustomTabsLaunchMode.EphemeralCustomTab
    }

    return when (unsupportedEphemeralCustomTabsFallback) {
        UnsupportedEphemeralCustomTabsFallback.PrivateWebView -> EphemeralCustomTabsLaunchMode.PrivateWebView
        UnsupportedEphemeralCustomTabsFallback.NormalCustomTab -> EphemeralCustomTabsLaunchMode.NormalCustomTab
    }
}
