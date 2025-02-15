package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest


actual class PlatformCodeAuthFlow(
    client: OpenIdConnectClient
) : CodeAuthFlow(client) {


    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        return AuthCodeResponse("code")
    }
}