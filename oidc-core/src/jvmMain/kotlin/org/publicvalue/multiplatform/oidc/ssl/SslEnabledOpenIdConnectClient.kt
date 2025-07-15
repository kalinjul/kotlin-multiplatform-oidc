package org.publicvalue.multiplatform.oidc.ssl

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.DefaultOpenIdConnectClient
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig

/**
 * JVM-specific factory functions for creating OpenIdConnectClient with SSL support
 */

/**
 * Create an OpenIdConnectClient with SSL configuration support (JVM only).
 * 
 * This function checks if the config contains SSL settings and creates an appropriately
 * configured HttpClient. If no SSL configuration is present, uses the default client.
 * 
 * Example usage:
 * ```kotlin
 * val client = createSslEnabledOpenIdConnectClient(config)
 * ```
 */
@ExperimentalOpenIdConnect
fun createSslEnabledOpenIdConnectClient(config: OpenIdConnectClientConfig): OpenIdConnectClient {
    val httpClient = if (config.hasSslConfig) {
        createSslConfiguredHttpClient(config.sslConfig!!)
    } else {
        DefaultOpenIdConnectClient.DefaultHttpClient
    }
    
    return DefaultOpenIdConnectClient(httpClient = httpClient, config = config)
}

/**
 * Create an OpenIdConnectClient with custom HttpClient and SSL configuration (JVM only).
 * 
 * This allows for full customization of the HttpClient while still applying SSL configuration.
 * 
 * Example usage:
 * ```kotlin
 * val client = createSslEnabledOpenIdConnectClient(config) { 
 *     // Custom HttpClient configuration
 *     install(SomeOtherPlugin)
 * }
 * ```
 */
@ExperimentalOpenIdConnect
fun createSslEnabledOpenIdConnectClient(
    config: OpenIdConnectClientConfig,
    httpClientConfig: io.ktor.client.HttpClientConfig<io.ktor.client.engine.okhttp.OkHttpConfig>.() -> Unit = {}
): OpenIdConnectClient {
    val httpClient = createSslConfiguredHttpClient(config.sslConfig, httpClientConfig)
    return DefaultOpenIdConnectClient(httpClient = httpClient, config = config)
}

/**
 * Create an HttpClient configured with SSL settings and default OIDC configuration.
 * 
 * This creates an HttpClient with:
 * - OkHttp engine (required for SSL configuration on JVM)
 * - ContentNegotiation with JSON support
 * - SSL configuration applied if provided
 * 
 * @param sslConfig SSL configuration to apply, null for default SSL behavior
 * @param additionalConfig Additional HttpClient configuration
 */
@ExperimentalOpenIdConnect
fun createSslConfiguredHttpClient(
    sslConfig: SslConfig? = null,
    additionalConfig: io.ktor.client.HttpClientConfig<io.ktor.client.engine.okhttp.OkHttpConfig>.() -> Unit = {}
): HttpClient {
    return HttpClient(OkHttp) {
        // Apply SSL configuration if provided
        sslConfig?.let { config ->
            JvmSslConfigurer.run {
                configureSsl(config)
            }
        }
        
        // Configure content negotiation (simplified for JVM)
        install(ContentNegotiation) {
            register(
                contentType = ContentType.Application.Json,
                converter = KotlinxSerializationConverter(
                    Json {
                        explicitNulls = false
                        ignoreUnknownKeys = true
                    }
                )
            )
        }
        
        // Apply additional configuration
        additionalConfig()
    }
}

/**
 * Extension function on OpenIdConnectClientConfig to create SSL-enabled client (JVM only).
 * 
 * Example usage:
 * ```kotlin
 * val config = OpenIdConnectClientConfig { 
 *     // ... configuration
 *     ssl { 
 *         trustStore("/path/to/truststore.jks", "password")
 *     }
 * }
 * val client = config.createSslEnabledClient()
 * ```
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.createSslEnabledClient(): OpenIdConnectClient {
    return createSslEnabledOpenIdConnectClient(this)
}

/**
 * Extension function to create SSL-enabled client with custom HttpClient configuration.
 */
@ExperimentalOpenIdConnect
fun OpenIdConnectClientConfig.createSslEnabledClient(
    httpClientConfig: io.ktor.client.HttpClientConfig<io.ktor.client.engine.okhttp.OkHttpConfig>.() -> Unit
): OpenIdConnectClient {
    return createSslEnabledOpenIdConnectClient(this, httpClientConfig)
}