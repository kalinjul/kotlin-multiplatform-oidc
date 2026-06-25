package org.publicvalue.multiplatform.oidc.appsupport

import kotlin.test.Test
import kotlin.test.assertEquals

class EphemeralCustomTabsPolicyTest {
    @Test
    fun `normal sessions use normal Custom Tabs even if ephemeral browsing is unsupported`() {
        val mode = resolveEphemeralCustomTabsLaunchMode(
            ephemeralSession = false,
            ephemeralBrowsingSupported = false,
            unsupportedEphemeralCustomTabsFallback = UnsupportedEphemeralCustomTabsFallback.PrivateWebView
        )

        assertEquals(EphemeralCustomTabsLaunchMode.NormalCustomTab, mode)
    }

    @Test
    fun `ephemeral sessions use ephemeral Custom Tabs when supported`() {
        val mode = resolveEphemeralCustomTabsLaunchMode(
            ephemeralSession = true,
            ephemeralBrowsingSupported = true,
            unsupportedEphemeralCustomTabsFallback = UnsupportedEphemeralCustomTabsFallback.PrivateWebView
        )

        assertEquals(EphemeralCustomTabsLaunchMode.EphemeralCustomTab, mode)
    }

    @Test
    fun `ephemeral sessions use private WebView fallback by default when unsupported`() {
        val mode = resolveEphemeralCustomTabsLaunchMode(
            ephemeralSession = true,
            ephemeralBrowsingSupported = false,
            unsupportedEphemeralCustomTabsFallback = UnsupportedEphemeralCustomTabsFallback.PrivateWebView
        )

        assertEquals(EphemeralCustomTabsLaunchMode.PrivateWebView, mode)
    }

    @Test
    fun `ephemeral sessions can opt into normal Custom Tab fallback when unsupported`() {
        val mode = resolveEphemeralCustomTabsLaunchMode(
            ephemeralSession = true,
            ephemeralBrowsingSupported = false,
            unsupportedEphemeralCustomTabsFallback = UnsupportedEphemeralCustomTabsFallback.NormalCustomTab
        )

        assertEquals(EphemeralCustomTabsLaunchMode.NormalCustomTab, mode)
    }
}
