package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

class IosAuthFlowFactory(
): AuthFlowFactory {
    override fun createAuthFlow(client: OpenIDConnectClient): PlatformOidcAuthFlow {
        return PlatformOidcAuthFlow(client)
    }
}