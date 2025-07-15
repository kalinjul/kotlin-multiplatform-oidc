package org.publicvalue.multiplatform.oidc.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig

/**
 * JVM-specific extensions for OpenIdConnectClientConfig to support SSL configuration
 */

/**
 * Configure SSL/TLS settings for the HTTP client (JVM only).
 * 
 * Example usage:
 * ```kotlin
 * OpenIdConnectClient(discoveryUri = "https://example.com/.well-known/openid-configuration") {
 *     clientId = "client-id"
 *     
 *     ssl {
 *         trustStore("/path/to/truststore.jks", "password")
 *         disableHostnameVerification()
 *     }
 * }
 * ```
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.ssl(block: SslConfigBuilder.() -> Unit) {
    val builder = SslConfigBuilder()
    builder.block()
    platformSpecificConfig = builder.build()
}

/**
 * Get the SSL configuration from the platform-specific config (JVM only).
 * Returns null if no SSL configuration is set.
 */
@ExperimentalOpenIdConnect
val OpenIdConnectClientConfig.sslConfig: SslConfig?
    get() = platformSpecificConfig as? SslConfig

/**
 * Set SSL configuration directly (JVM only).
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.setSslConfig(sslConfig: SslConfig) {
    platformSpecificConfig = sslConfig
}

/**
 * Check if SSL configuration is present (JVM only).
 */
@ExperimentalOpenIdConnect
val OpenIdConnectClientConfig.hasSslConfig: Boolean
    get() = sslConfig != null

/**
 * Configure SSL with a pre-built SslConfig (JVM only).
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.ssl(sslConfig: SslConfig) {
    platformSpecificConfig = sslConfig
}

/**
 * Configure SSL to disable all validation (UNSAFE - testing only).
 * 
 * Example:
 * ```kotlin
 * config.unsafeSsl()
 * ```
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.unsafeSsl() {
    platformSpecificConfig = SslConfig.unsafe()
}

/**
 * Configure SSL with custom trust store.
 * 
 * Example:
 * ```kotlin
 * config.sslWithTrustStore("/path/to/truststore.jks", "password")
 * ```
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.sslWithTrustStore(
    path: String, 
    password: String? = null, 
    type: String? = null
) {
    platformSpecificConfig = SslConfig.withTrustStore(path, password, type)
}

/**
 * Configure SSL with client certificate authentication.
 * 
 * Example:
 * ```kotlin
 * config.sslWithClientCertificate(
 *     keyStorePath = "/path/to/keystore.p12",
 *     keyStorePassword = "password",
 *     trustStorePath = "/path/to/truststore.jks"
 * )
 * ```
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.sslWithClientCertificate(
    keyStorePath: String,
    keyStorePassword: String? = null,
    keyStoreType: String? = null,
    trustStorePath: String? = null,
    trustStorePassword: String? = null,
    trustStoreType: String? = null
) {
    platformSpecificConfig = SslConfig.withClientCertificate(
        keyStorePath = keyStorePath,
        keyStorePassword = keyStorePassword,
        keyStoreType = keyStoreType,
        trustStorePath = trustStorePath,
        trustStorePassword = trustStorePassword,
        trustStoreType = trustStoreType
    )
}