package org.publicvalue.multiplatform.oidc.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig

/**
 * JVM-specific override of OpenIdConnectClient builder that automatically applies SSL configuration.
 * 
 * This function automatically detects if SSL configuration is present in the config and creates
 * an appropriately configured HTTP client. On JVM, this replaces the common OpenIdConnectClient
 * builder to provide SSL support.
 * 
 * Example usage:
 * ```kotlin
 * val client = OpenIdConnectClient(discoveryUri = "https://example.com/.well-known/openid-configuration") {
 *     clientId = "client-id"
 *     clientSecret = "client-secret"
 *     
 *     ssl {
 *         trustStore("/path/to/truststore.jks", "password")
 *         disableHostnameVerification()
 *     }
 * }
 * ```
 * 
 * @param discoveryUri if set, endpoints in the configuration are optional.
 * Setting an endpoint manually will override a discovered endpoint.
 * @param block configuration closure. See [OpenIdConnectClientConfig]
 */
@ExperimentalOpenIdConnect
@Suppress("UNUSED") // This function shadows the common one on JVM
fun OpenIdConnectClient(
    discoveryUri: String? = null,
    block: OpenIdConnectClientConfig.() -> Unit
): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig(discoveryUri).apply(block)
    
    // Automatically use SSL-enabled client if SSL configuration is present
    return if (config.hasSslConfig) {
        createSslEnabledOpenIdConnectClient(config)
    } else {
        // Use the default implementation for backward compatibility
        org.publicvalue.multiplatform.oidc.DefaultOpenIdConnectClient(config = config)
    }
}

/**
 * JVM-specific OpenIdConnectClient builder with explicit SSL configuration.
 * 
 * This variant allows you to pass SSL configuration directly without using the DSL.
 * 
 * Example usage:
 * ```kotlin
 * val sslConfig = SslConfig.withTrustStore("/path/to/truststore.jks", "password")
 * val client = OpenIdConnectClient(discoveryUri = "https://example.com/.well-known/openid-configuration", sslConfig = sslConfig) {
 *     clientId = "client-id"
 *     clientSecret = "client-secret"
 * }
 * ```
 * 
 * @param discoveryUri discovery endpoint URL
 * @param sslConfig SSL configuration to apply
 * @param block configuration closure
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClient(
    discoveryUri: String? = null,
    sslConfig: SslConfig,
    block: OpenIdConnectClientConfig.() -> Unit
): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig(discoveryUri).apply {
        setSslConfig(sslConfig)
        block()
    }
    
    return createSslEnabledOpenIdConnectClient(config)
}

/**
 * JVM-specific OpenIdConnectClient builder with advanced HTTP client customization.
 * 
 * This variant allows full customization of the underlying HTTP client while still
 * applying SSL configuration.
 * 
 * Example usage:
 * ```kotlin
 * val client = OpenIdConnectClientWithCustomHttp(
 *     discoveryUri = "https://example.com/.well-known/openid-configuration",
 *     httpClientConfig = {
 *         install(SomePlugin) {
 *             // plugin configuration
 *         }
 *     }
 * ) {
 *     clientId = "client-id"
 *     ssl {
 *         trustStore("/path/to/truststore.jks", "password")
 *     }
 * }
 * ```
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientWithCustomHttp(
    discoveryUri: String? = null,
    httpClientConfig: io.ktor.client.HttpClientConfig<io.ktor.client.engine.okhttp.OkHttpConfig>.() -> Unit,
    block: OpenIdConnectClientConfig.() -> Unit
): OpenIdConnectClient {
    val config = OpenIdConnectClientConfig(discoveryUri).apply(block)
    return createSslEnabledOpenIdConnectClient(config, httpClientConfig)
}