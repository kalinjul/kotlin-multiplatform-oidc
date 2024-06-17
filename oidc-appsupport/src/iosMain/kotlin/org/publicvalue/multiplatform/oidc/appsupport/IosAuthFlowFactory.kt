package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Suppress("unused")
class IosAuthFlowFactory(
    private val ephemeralBrowserSession: Boolean = false
): AuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformAuthFlow {
        return PlatformAuthFlow(client, ephemeralBrowserSession)
    }
}