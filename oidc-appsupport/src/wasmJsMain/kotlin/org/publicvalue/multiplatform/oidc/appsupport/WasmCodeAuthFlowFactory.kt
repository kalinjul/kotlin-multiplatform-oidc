package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import kotlin.experimental.ExperimentalObjCRefinement


class WasmCodeAuthFlowFactory: CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(client)
    }
}