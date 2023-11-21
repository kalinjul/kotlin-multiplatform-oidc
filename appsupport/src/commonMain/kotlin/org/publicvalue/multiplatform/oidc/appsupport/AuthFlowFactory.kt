package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

interface AuthFlowFactory {
    fun createAuthFlow(client: OpenIDConnectClient): OidcAuthFlow
}