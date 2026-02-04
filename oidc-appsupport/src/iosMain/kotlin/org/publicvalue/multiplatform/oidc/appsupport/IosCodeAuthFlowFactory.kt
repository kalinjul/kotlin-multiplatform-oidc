package org.publicvalue.multiplatform.oidc.appsupport

import kotlinx.coroutines.runBlocking
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class, ExperimentalObjCName::class)
@ObjCName("CodeAuthFlowFactory", "CodeAuthFlowFactory", exact = true)
@Suppress("unused")
class IosCodeAuthFlowFactory(
    private val ephemeralBrowserSession: Boolean = false,
    /** factory used to create preferences to save session information during login process. **/
    private val preferencesFactory: PreferencesFactory = PreferencesFactory()
): CodeAuthFlowFactory {
    private val preferences = runBlocking { preferencesFactory.getOrCreate(PREFERENCES_FILENAME) }

    // constructor for swift-only library
    constructor(ephemeralBrowserSession: Boolean) : this(ephemeralBrowserSession, preferencesFactory = PreferencesFactory())

    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(
            client = client,
            preferences = preferences,
            webFlow = createWebFlow(),
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return PlatformEndSessionFlow(
            client = client,
            preferences = preferences,
            webFlow = createWebFlow(),
        )
    }

    private fun createWebFlow(): WebSessionFlow = WebSessionFlow(
        ephemeralBrowserSession = ephemeralBrowserSession,
        preferences = preferences
    )
}