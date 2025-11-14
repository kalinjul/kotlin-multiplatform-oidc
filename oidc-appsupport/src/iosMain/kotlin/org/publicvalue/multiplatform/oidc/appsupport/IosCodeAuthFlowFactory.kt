package org.publicvalue.multiplatform.oidc.appsupport

import kotlinx.coroutines.runBlocking
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Suppress("unused")
class IosCodeAuthFlowFactory(
    private val ephemeralBrowserSession: Boolean = false,
    /** factory used to create preferences to save session information during login process. **/
    private val preferencesFactory: PreferencesFactory = PreferencesFactory()
): CodeAuthFlowFactory {
    private val preferences = runBlocking { preferencesFactory.getOrCreate(PREFERENCES_FILENAME) }
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(
            client = client,
            webFlow = WebSessionFlow(
                ephemeralBrowserSession = ephemeralBrowserSession,
                preferences = preferences
            ),
            preferences = preferences
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}