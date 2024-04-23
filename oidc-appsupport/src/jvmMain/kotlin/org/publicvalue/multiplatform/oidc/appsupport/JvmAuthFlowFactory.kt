package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient

@Suppress("unused")
class JvmAuthFlowFactory(
): AuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformAuthFlow {
        return PlatformAuthFlow(client)
    }
}