package org.publicvalue.multiplatform.oidc.appsupport

/**
 * Fallback behavior for Android authorization flows when [AndroidCodeAuthFlowFactory] requests an
 * ephemeral session but the selected Custom Tabs provider does not support ephemeral browsing.
 */
enum class UnsupportedEphemeralCustomTabsFallback {
    /**
     * Use the embedded WebView path and clear WebView state before and after the session.
     *
     * Android WebView does not provide the same isolated ephemeral browser profile as supported
     * ephemeral Custom Tabs. This is the privacy-preserving fallback available to the library.
     */
    PrivateWebView,

    /**
     * Launch a normal Custom Tab even though ephemeral Custom Tabs are unsupported.
     *
     * This may reuse the user's existing browser cookies and SSO session even when
     * `ephemeralSession` is `true`.
     */
    NormalCustomTab,
}
