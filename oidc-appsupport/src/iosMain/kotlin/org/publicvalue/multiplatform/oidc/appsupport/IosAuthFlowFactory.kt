package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import kotlin.experimental.ExperimentalObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthFlowFactory", name = "AuthFlowFactory")
class IosAuthFlowFactory(
): AuthFlowFactory {
    companion object {
        val instance by lazy { IosAuthFlowFactory() }
    }
    override fun createAuthFlow(client: OpenIDConnectClient): PlatformOidcCodeAuthFlow {
        return PlatformOidcCodeAuthFlow(client)
    }
}