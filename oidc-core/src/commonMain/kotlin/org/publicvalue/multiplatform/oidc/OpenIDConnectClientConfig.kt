package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

@EndpointMarker
class OpenIDConnectClientConfig(
    var endpoints: Endpoints? = null,
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
        this.endpoints = Endpoints().apply(block)
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