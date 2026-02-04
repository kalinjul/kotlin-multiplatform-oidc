package org.publicvalue.multiplatform.oidc.flows

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCRefinement::class, ExperimentalObjCName::class)
@ObjCName("CodeAuthFlowFactoryProtocol", "CodeAuthFlowFactoryProtocol", exact = true)
interface CodeAuthFlowFactory {
    /**
     * Create an auth flow to perform authorization.
     */
    fun createAuthFlow(client: OpenIdConnectClient): CodeAuthFlow

    /**
     * Create a flow to perform session termination.
     */
    fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow
}