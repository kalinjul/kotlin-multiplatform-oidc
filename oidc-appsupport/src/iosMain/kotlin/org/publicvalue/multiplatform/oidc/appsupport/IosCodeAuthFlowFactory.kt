package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Suppress("unused")
class IosCodeAuthFlowFactory(
    private val ephemeralBrowserSession: Boolean = false
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(
            client = client,
            webFlow = WebSessionFlow(
                ephemeralBrowserSession = ephemeralBrowserSession
            )
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}