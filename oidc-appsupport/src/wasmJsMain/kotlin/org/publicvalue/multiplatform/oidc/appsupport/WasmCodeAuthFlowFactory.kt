package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient

@ExperimentalOpenIdConnect
class WasmCodeAuthFlowFactory(
    private val windowTarget: String = "DialogFenster",
    private val windowFeatures: String = "width=1000,height=800,resizable=yes,scrollbars=yes",
    private val redirectOrigin: String
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(client, windowTarget, windowFeatures, redirectOrigin)
    }
}