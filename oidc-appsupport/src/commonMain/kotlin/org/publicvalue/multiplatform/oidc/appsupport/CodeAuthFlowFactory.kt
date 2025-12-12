package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import kotlin.experimental.ExperimentalObjCRefinement

@OptIn(ExperimentalObjCRefinement::class)
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