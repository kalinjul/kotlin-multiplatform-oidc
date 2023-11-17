package org.publicvalue.multiplatform.oauth.domain

import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.data.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

fun Client.createOidcClient(idp: Identityprovider): OpenIDConnectClient {
    return OpenIDConnectClient {
        endpoints {
            tokenEndpoint(idp.endpointToken ?: "")
            authEndpoint(idp.endpointAuthorization ?: "")
        }
        clientId(client_id ?: "")
        clientSecret(client_secret ?: "")
        scope?.let { scope(it) }
        redirectUri(Constants.REDIRECT_URL)
        codeChallengeMethod(this@createOidcClient.code_challenge_method.toLibrary())
    }
}

private fun CodeChallengeMethod.toLibrary(): org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod {
    return when (this) {
        CodeChallengeMethod.S256 -> org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod.S256
        CodeChallengeMethod.plain -> org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod.plain
        CodeChallengeMethod.off -> org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod.off
    }
}
