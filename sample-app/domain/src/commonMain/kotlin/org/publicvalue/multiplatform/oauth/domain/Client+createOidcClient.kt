package org.publicvalue.multiplatform.oauth.domain

import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient

fun Client.createOidcClient(idp: Identityprovider): OpenIDConnectClient {
    return OpenIDConnectClient {
        endpoints {
            tokenEndpoint(idp.endpointToken ?: "")
            authEndpoint(idp.endpointAuthorization ?: "")
        }
        clientId(client_id ?: "")
        clientSecret(client_secret ?: "")
        scope(scope ?: "")
        redirectUri(Constants.REDIRECT_URL)
    }
}