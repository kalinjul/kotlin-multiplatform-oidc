package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.OpenIDConnectException
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.OidcCodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

actual class PlatformOidcCodeAuthFlow(
    client: OpenIDConnectClient
) : OidcCodeAuthFlow(client) {
    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        throw NotImplementedError("AppSupport is not available for desktop")
    }
}