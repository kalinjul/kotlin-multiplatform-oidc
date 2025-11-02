package org.publicvalue.multiplatform.oidc

import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oidc.types.remote.OpenIdConnectConfiguration
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Builds an [OpenIdConnectClientConfig] using the [block] parameter.
 *
 * @return [OpenIdConnectClientConfig]
 */
public fun OpenIdConnectClientConfig(block: OpenIdConnectClientConfig.() -> Unit): OpenIdConnectClientConfig {
    val config = OpenIdConnectClientConfig()
    config.block()
    return config
}

/**
 * Configuration for an [OpenIdConnectClient].
 * A configuration can also be built using [OpenIdConnectClient] builder function with block
 * argument.
 */
@OptIn(ExperimentalObjCName::class)
@EndpointMarker
@ObjCName(swiftName = "OpenIdConnectClientConfig", name = "OpenIdConnectClientConfig", exact = true)
@Suppress("LongParameterList")
public class OpenIdConnectClientConfig(
    /**
     * If set, no further endpoints have to be configured.
     * You can override discovered endpoints in [endpoints]
     */
    public val discoveryUri: String? = null,
    public var endpoints: Endpoints? = null,
    /**
     * REQUIRED
     *
     * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-2.2)
     */
    public var clientId: String? = null,
    public var clientSecret: String? = null,
    /**
     * OPTIONAL
     *
     * [RFC6749](https://datatracker.ietf.org/doc/html/rfc6749#section-3.3)
     */
    public var scope: String? = null,
    /**
     * The Code Challenge Method to use for PKCE.
     *
     * Default is [S256][CodeChallengeMethod.S256]).
     * Set to [off][CodeChallengeMethod.off]) to disable PKCE.
     */
    public var codeChallengeMethod: CodeChallengeMethod = CodeChallengeMethod.S256,
    /**
     * [rfc6749](https://datatracker.ietf.org/doc/html/rfc6749#section-3.1.2)
     */
    public var redirectUri: String? = null,

    /**
     * Url that is used for redirecting back to the app after logout request.
     * Is only used if endSession is called on
     * an [EndSessionFlow][org.publicvalue.multiplatform.oidc.flows.EndSessionFlow].
     *
     * [OpenID Spec](https://openid.net/specs/openid-connect-rpinitiated-1_0.html)
     */
    public var postLogoutRedirectUri: String? = null,

    /**
     * Disables sending nonce in the authentication request.
     * This is not recommended as [nonce is used to mitigate replay attacks](https://openid.net/specs/openid-connect-core-1_0.html#NonceNotes).
     */
    public var disableNonce: Boolean = false
) {
    /**
     * Configure the endpoints.
     *
     * Either [discoveryUri] or both [authorizationEndpoint][Endpoints.authorizationEndpoint] and
     * [tokenEndpoint][Endpoints.tokenEndpoint] are required.
     *
     * [endSessionEndpoint][Endpoints.endSessionEndpoint] is required if you wish to logout.
     */
    public fun endpoints(
        block: Endpoints.() -> Unit
    ) {
        this.endpoints = (endpoints ?: Endpoints()).apply(block)
    }

    /**
     * Update this client config with discovery document.
     * Will NOT override already set properties in config.
     */
    public fun updateWithDiscovery(config: OpenIdConnectConfiguration) {
        endpoints {
            authorizationEndpoint = authorizationEndpoint ?: config.authorizationEndpoint
            tokenEndpoint = tokenEndpoint ?: config.tokenEndpoint
            endSessionEndpoint = endSessionEndpoint ?: config.endSessionEndpoint
            userInfoEndpoint = userInfoEndpoint ?: config.userinfoEndpoint
            revocationEndpoint = revocationEndpoint ?: config.revocationEndpoint
        }
        this.scope = scope ?: config.scopesSupported?.joinToString(" ")
    }
}

@DslMarker
private annotation class EndpointMarker

/**
 * Endpoint configuration
 */
@OptIn(ExperimentalObjCName::class)
@EndpointMarker
@ObjCName(swiftName = "Endpoints", name = "Endpoints", exact = true)
public data class Endpoints(
    public var tokenEndpoint: String? = null,
    public var authorizationEndpoint: String? = null,
    public var userInfoEndpoint: String? = null,
    public var endSessionEndpoint: String? = null,
    public var revocationEndpoint: String? = null
) {
    /**
     * Set a baseUrl that is applied for all endpoints.
     * E.g. baseUrl("http://localhost/oauth/") {
     *      tokenEndpoint = "token"
     * }
     */
    @Suppress("unused")
    public fun baseUrl(baseUrl: String, block: Endpoints.() -> Unit) {
        val endpoints = Endpoints()
        endpoints.block()
        tokenEndpoint = baseUrl + endpoints.tokenEndpoint
        authorizationEndpoint = baseUrl + endpoints.authorizationEndpoint
        endSessionEndpoint = baseUrl + endpoints.endSessionEndpoint
        revocationEndpoint = baseUrl + endpoints.revocationEndpoint
    }
}

/**
 * Validate the config
 *
 * @receiver the [OpenIdConnectClientConfig] to validate
 * @throws OpenIdConnectException if the config is invalid
 */
public fun OpenIdConnectClientConfig.validate() {
    if (discoveryUri.isNullOrBlank()) {
        if (endpoints?.tokenEndpoint == null) {
            throw OpenIdConnectException.InvalidUrl("Invalid configuration: tokenEndpoint is null")
        }
        if (endpoints?.authorizationEndpoint == null) {
            throw OpenIdConnectException.InvalidUrl("Invalid configuration: authorizationEndpoint is null")
        }
    }
    if (clientId == null) {
        throw OpenIdConnectException.InvalidUrl("Invalid configuration: clientId is null")
    }
}
