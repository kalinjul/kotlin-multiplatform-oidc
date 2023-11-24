package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.flows.OidcCodeAuthFlow

interface AuthFlowFactory {
    fun createAuthFlow(client: OpenIDConnectClient): OidcCodeAuthFlow
}