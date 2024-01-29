package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient

@Suppress("unused")
class JvmCodeAuthFlowFactory(
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(client)
    }
}