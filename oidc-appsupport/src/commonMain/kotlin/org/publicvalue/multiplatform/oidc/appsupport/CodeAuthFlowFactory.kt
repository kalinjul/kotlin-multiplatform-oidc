package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
interface CodeAuthFlowFactory {
    fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow
    fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow
}