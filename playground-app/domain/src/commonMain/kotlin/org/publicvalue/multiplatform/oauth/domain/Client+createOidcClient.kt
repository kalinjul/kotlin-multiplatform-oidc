package org.publicvalue.multiplatform.oauth.domain

import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.data.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient

fun Client.createOidcClient(idp: Identityprovider): OpenIdConnectClient {
    return OpenIdConnectClient {
        endpoints {
            tokenEndpoint = idp.endpointToken
            authorizationEndpoint = idp.endpointAuthorization
            endSessionEndpoint = idp.endpointEndSession
        }
        clientId = this@createOidcClient.client_id
        clientSecret = this@createOidcClient.client_secret
        this.scope = this@createOidcClient.scope
        redirectUri = Constants.REDIRECT_URL
        postLogoutRedirectUri = Constants.POST_LOGOUT_REDIRECT_URL
        codeChallengeMethod = this@createOidcClient.code_challenge_method.toLibrary()
    }
}

private fun CodeChallengeMethod.toLibrary(): org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod {
    return when (this) {
        CodeChallengeMethod.S256 -> org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod.S256
        CodeChallengeMethod.plain -> org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod.plain
        CodeChallengeMethod.off -> org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod.off
    }
}
