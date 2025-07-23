package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import java.io.File

/**
 * JVM-specific factory for creating OAuth authorization flows.
 * 
 * Supports both HTTP and HTTPS local redirect servers for OAuth redirects.
 * 
 * Example usage:
 * ```kotlin
 * // HTTP (default)
 * val factory = JvmCodeAuthFlowFactory()
 * 
 * // HTTPS with custom port
 * val httpsFactory = JvmCodeAuthFlowFactory(
 *     port = 8443,
 *     webserverProvider = { SslWebserver(certificateSource = CertificateSourceFactory.selfSigned()) }
 * )
 * 
 * // Custom port and webserver
 * val customFactory = JvmCodeAuthFlowFactory(port = 9090) { SslWebserver(enableHttps = true) }
 * ```
 */
@ExperimentalOpenIdConnect
class JvmCodeAuthFlowFactory(
    private val port: Int = 8080,
    private val webserverProvider: () -> Webserver = { SimpleKtorWebserver() }
): CodeAuthFlowFactory {
    
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(client, webserverProvider = webserverProvider, port = port)
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}