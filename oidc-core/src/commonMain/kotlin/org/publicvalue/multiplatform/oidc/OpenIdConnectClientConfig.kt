package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.remote.OpenIdConnectConfiguration
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@EndpointMarker
@ObjCName(swiftName = "OpenIdConnectClientConfig", name = "OpenIdConnectClientConfig", exact = true)
class OpenIdConnectClientConfig(
    /**
     * If set, no further endpoints have to be configured.
     * You can override discovered endpoints in [endpoints]
     */
    val discoveryUri: String? = null,
    var endpoints: Endpoints = Endpoints(),
    /**
     * REQUIRED
     *
     * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-2.2)
     */
    var clientId: String? = null,
    var clientSecret: String? = null,
    /**
     * OPTIONAL
     *
     * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-3.3)
     */
    var scope: String? = null,
    /**
     * The Code Challenge Method to use for PKCE.
     *
     * Default is [S256][CodeChallengeMethod.S256]).
     * Set to [S256][CodeChallengeMethod.off]) to disable PKCE.
     */
    var codeChallengeMethod: CodeChallengeMethod = CodeChallengeMethod.S256,
    /**
     * https://datatracker.ietf.org/doc/html/rfc6749#section-3.1.2
     */
    var redirectUri: String? = null,
) {
    /**
     * Configure the endpoints.
     *
     * Either [discoveryUri] or both [authorizationEndpoint][Endpoints.authorizationEndpoint] and
     * [tokenEndpoint][Endpoints.tokenEndpoint] are required.
     *
     * [endSessionEndpoint][Endpoints.endSessionEndpoint] is required if you wish to logout.
     */
    fun endpoints(
        block: Endpoints.() -> Unit
    ) {
        this.endpoints = endpoints.apply(block)
    }

    /**
     * Update this client config with discovery document.
     * Will NOT override already set properties in config.
     */
    fun updateWithDiscovery(config: OpenIdConnectConfiguration) {
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

@OptIn(ExperimentalObjCName::class)
@EndpointMarker
@ObjCName(swiftName = "Endpoints", name = "Endpoints", exact = true)
data class Endpoints(
    var tokenEndpoint: String? = null,
    var authorizationEndpoint: String? = null,
    var userInfoEndpoint: String? = null,
    var endSessionEndpoint: String? = null
) {
    /**
     * Set a baseUrl that is applied for all endpoints.
     * E.g. baseUrl("http://localhost/oauth/") {
     *      tokenEndpoint = "token"
     * }
     */
    @Suppress("unused")
    fun baseUrl(baseUrl: String, block: Endpoints.() -> Unit) {
        val endpoints = Endpoints()
        endpoints.block()
        tokenEndpoint = baseUrl + endpoints.tokenEndpoint
        authorizationEndpoint = baseUrl + endpoints.authorizationEndpoint
        endSessionEndpoint = baseUrl + endpoints.endSessionEndpoint
    }
}

fun OpenIdConnectClientConfig.validate() {
    if (discoveryUri.isNullOrBlank()) {
        if (endpoints.tokenEndpoint == null) {
            throw OpenIdConnectException.InvalidUrl("Invalid configuration: tokenEndpoint is null")
        }
        if (endpoints.authorizationEndpoint == null) {
            throw OpenIdConnectException.InvalidUrl("Invalid configuration: authorizationEndpoint is null")
        }
    }
    if (clientId == null) {
        throw OpenIdConnectException.InvalidUrl("Invalid configuration: clientId is null")
    }
}