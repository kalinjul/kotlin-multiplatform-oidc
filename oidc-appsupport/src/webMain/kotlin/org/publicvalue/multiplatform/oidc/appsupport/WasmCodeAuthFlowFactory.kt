package org.publicvalue.multiplatform.oidc.appsupport

import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory

@ExperimentalOpenIdConnect
class WasmCodeAuthFlowFactory(
    private val windowTarget: String = "",
    private val windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    private val redirectOrigin: String = window.location.origin,
    /** factory used to create preferences to save session information during login process. **/
    private val preferencesFactory: PreferencesFactory = PreferencesFactory()
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        val preferences = preferencesFactory.create()
        return PlatformCodeAuthFlow(windowTarget, windowFeatures, redirectOrigin, client, preferences)
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}