package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCRefinement::class, ExperimentalObjCName::class)
@ObjCName("CodeAuthFlowFactoryProtocol", "CodeAuthFlowFactoryProtocol", exact = true)
interface CodeAuthFlowFactory {
    fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow
    fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow
}