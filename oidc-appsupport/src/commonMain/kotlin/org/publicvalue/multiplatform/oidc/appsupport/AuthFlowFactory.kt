package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.flows.OidcCodeAuthFlow
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthFlowFactoryProtocol", name = "AuthFlowFactoryProtocol", exact = true)
interface AuthFlowFactory {
    fun createAuthFlow(client: OpenIDConnectClient): OidcCodeAuthFlow
}