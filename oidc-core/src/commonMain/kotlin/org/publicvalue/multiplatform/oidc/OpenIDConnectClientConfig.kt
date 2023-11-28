package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.remote.OpenIDConnectConfiguration

@EndpointMarker
class OpenIDConnectClientConfig(
    var discoveryUri: String? = null,
    var endpoints: Endpoints = Endpoints(),
    /**
     * REQUIRED
     * https://datatracker.ietf.org/doc/html/rfc6749#section-2.2
     */
    var clientId: String? = null,
    var clientSecret: String? = null,
    /**
     * OPTIONAL
     * https://datatracker.ietf.org/doc/html/rfc6749#section-3.3
     */
    var scope: String? = null,
    var codeChallengeMethod: CodeChallengeMethod = CodeChallengeMethod.off,
    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-3.1.2
     */
    var redirectUri: String? = null,
) {
    fun endpoints(
        block: Endpoints.() -> Unit
    ) {
        this.endpoints = endpoints.apply(block)
    }

    /**
     * Update this client config with discovery document.
     * Will NOT override already set properties in config.
     */
    fun updateWithDiscovery(config: OpenIDConnectConfiguration) {
        endpoints {
            authorizationEndpoint = authorizationEndpoint ?: config.authorization_endpoint
            tokenEndpoint = tokenEndpoint ?: config.token_endpoint
            endSessionEndpoint = endSessionEndpoint ?: config.end_session_endpoint
            userInfoEndpoint = userInfoEndpoint ?: config.userinfo_endpoint
        }
        this.scope = scope ?: config.scopes_supported?.joinToString(" ")
    }
}

@DslMarker
annotation class EndpointMarker

@EndpointMarker
data class Endpoints(
    var tokenEndpoint: String? = null,
    var authorizationEndpoint: String? = null,
    var userInfoEndpoint: String? = null,
    var endSessionEndpoint: String? = null
) {
    fun baseUrl(baseUrl: String, block: Endpoints.() -> Unit) {
        val endpoints = Endpoints()
        endpoints.block()
        tokenEndpoint = baseUrl + endpoints.tokenEndpoint
        authorizationEndpoint = baseUrl + endpoints.authorizationEndpoint
        endSessionEndpoint = baseUrl + endpoints.endSessionEndpoint
    }
}

fun OpenIDConnectClientConfig.validate() {
    if (discoveryUri == null) {
        if (endpoints.tokenEndpoint == null) {
            throw OpenIDConnectException.InvalidUrl("Invalid configuration: tokenEndpoint is null")
        }
        if (endpoints.authorizationEndpoint == null) {
            throw OpenIDConnectException.InvalidUrl("Invalid configuration: authorizationEndpoint is null")
        }
    }

    if (clientId == null) {
        throw OpenIDConnectException.InvalidUrl("Invalid configuration: clientId is null")
    }
}