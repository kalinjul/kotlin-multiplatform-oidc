package org.publicvalue.multiplatform.oidc.sample.home

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

actual suspend fun login(authFlowFactory: CodeAuthFlowFactory, client: OpenIdConnectClient, updateTokenResponse: suspend (AccessTokenResponse) -> Unit) {
    val newTokens = authFlowFactory.createAuthFlow(client).getAccessToken(
        configureAuthUrl = {
            parameters.append("prompt", "login")
        }
    )
    updateTokenResponse(newTokens)
}

actual suspend fun redirect(
    client: OpenIdConnectClient,
    state: String,
    code: String,
    updateTokenResponse: suspend (AccessTokenResponse) -> Unit
) {
}