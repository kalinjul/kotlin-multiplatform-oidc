package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

class AndroidAuthFlowFactory(
    val context: Context
): AuthFlowFactory {
    override fun createAuthFlow(client: OpenIDConnectClient): OidcAuthFlow {
        return PlatformOidcAuthFlow(
            context,
            client
        )
    }
}
