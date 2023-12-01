package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
@Suppress("unused")
class IosAuthFlowFactory(
): AuthFlowFactory {
    override fun createAuthFlow(client: OpenIDConnectClient): PlatformOidcCodeAuthFlow {
        return PlatformOidcCodeAuthFlow(client)
    }
}