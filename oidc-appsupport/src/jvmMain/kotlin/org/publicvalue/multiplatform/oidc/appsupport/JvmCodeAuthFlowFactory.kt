package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient

@Suppress("unused")
class JvmCodeAuthFlowFactory(
): AuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(client)
    }
}