package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.engine.EngineSSLConnectorBuilder
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateUtils
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSource
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSourceFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout

/**
 * SSL-enabled webserver for handling OAuth redirects over HTTPS.
 * 
 * This webserver creates a local HTTPS server using configurable certificate sources.
 * Browsers will show a security warning for self-signed certificates, but the connection will be encrypted.
 * 
 * Example usage:
 * ```kotlin
 * val webserver = SslWebserver(certificateSource = CertificateSourceFactory.selfSigned())
 * val httpServer = SslWebserver(enableHttps = false)
 * ```
 * 
 * This class handles:
 * - HTTP and HTTPS server startup
 * - Certificate management and SSL configuration
 * - OAuth redirect handling and response generation
 * - Server lifecycle management
 */

@ExperimentalOpenIdConnect
class SslWebserver(
    /**
     * Enable HTTPS for the local redirect server.
     * If false, falls back to HTTP behavior.
     */
    val enableHttps: Boolean = true,
    
    /**
     * Certificate source for providing SSL certificates.
     * If null, will use self-signed certificates.
     */
    val certificateSource: CertificateSource? = null,
    
    /**
     * Custom response to send after successful OAuth redirect.
     */
    val createResponse: suspend RoutingContext.() -> Unit = {
        call.respondText(
            status = HttpStatusCode.OK,
            text = """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8">
                    <title>Authorization redirect successful</title>
                  </head>
                  <body>
                    <h1>You may now close this page and return to your app.</h1>
                    <p><small>This connection is secured with SSL certificate.</small></p>
                  </body>
                </html>
            """.trimIndent(),
            contentType = ContentType.parse("text/html")
        )
    }
): Webserver {
    private var server: io.ktor.server.engine.ApplicationEngine? = null
    private var certificateInfo: CertificateUtils.CertificateInfo? = null
    private var redirectReceived: CompletableDeferred<ApplicationRequest>? = null
    
    // Resolved certificate source (default to self-signed if none provided)
    private val resolvedCertificateSource: CertificateSource by lazy {
        certificateSource ?: CertificateSourceFactory.selfSigned()
    }
    
    override suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): Url {
        start(port, redirectPath)
        return waitForRedirect()
    }

    override suspend fun start(port: Int, redirectPath: String) {
        server?.stop()
        
        // Create deferred for redirect capture
        redirectReceived = CompletableDeferred<ApplicationRequest>()
        
        if (enableHttps) {
            startHttpsServer(port, redirectPath)
        } else {
            startHttpServer(port, redirectPath)
        }
        
        // Wait for server to be actually ready (not arbitrary delay)
        ServerReadinessChecker.waitForServerReady(port)
    }

    override suspend fun waitForRedirect(): Url {
        // Wait for redirect with timeout (5 minutes)
        val call = withTimeout(300_000) {
            redirectReceived!!.await()
        }
        
        // Cleanup
        server?.stop()
        
        return Url(call.uri)
    }
    
    override suspend fun stop() {
        server?.stop()
    }
    
    
    /**
     * Start an HTTPS server with SSL certificate
     */
    private suspend fun startHttpsServer(
        port: Int, 
        redirectPath: String
    ) {
        val certInfo = getOrCreateCertificate()
        
        try {
            val serverEngine = embeddedServer(
                factory = Netty,
                configure = {
                    sslConnector(
                        keyStore = certInfo.keyStore,
                        keyAlias = "localhost",
                        keyStorePassword = { "localhost".toCharArray() },
                        privateKeyPassword = { "localhost".toCharArray() }
                    ) {
                        this.port = port
                        this.host = "localhost"
                    }
                }
            ) {
                routing {
                    get(redirectPath) {
                        createResponse()
                        redirectReceived?.complete(this.call.request)
                    }
                }
            }
            
            server = serverEngine.engine
            serverEngine.start(wait = false)
            
        } catch (e: Exception) {
            server?.stop()
            throw e
        }
    }
    
    /**
     * Start an HTTP server (fallback when HTTPS is disabled)
     */
    private fun startHttpServer(
        port: Int, 
        redirectPath: String
    ) {
        try {
            val serverEngine = embeddedServer(CIO, port = port) {
                routing {
                    get(redirectPath) {
                        createResponse()
                        redirectReceived?.complete(this.call.request)
                    }
                }
            }
            
            server = serverEngine.engine
            serverEngine.start(wait = false)
            
        } catch (e: Exception) {
            server?.stop()
            throw e
        }
    }
    
    /**
     * Get or create SSL certificate using the configured certificate source.
     */
    private suspend fun getOrCreateCertificate(): CertificateUtils.CertificateInfo {
        certificateInfo?.let { return it }
        
        val certInfo = try {
            resolvedCertificateSource.getCertificate("localhost")
        } catch (e: Exception) {
            throw RuntimeException("Failed to obtain SSL certificate from ${resolvedCertificateSource.displayName}. " +
                    "Consider using a different certificate source or disabling HTTPS.", e)
        }
        
        certificateInfo = certInfo
        return certInfo
    }
    
    /**
     * Get the protocol scheme this webserver uses.
     * 
     * @return "https" if HTTPS is enabled, "http" otherwise
     */
    val scheme: String
        get() = if (enableHttps) "https" else "http"
    
    /**
     * Check if the webserver is configured for HTTPS.
     * 
     * @return true if HTTPS is enabled, false for HTTP-only
     */
    val isHttpsEnabled: Boolean
        get() = enableHttps
    
    /**
     * Get information about the current SSL certificate.
     * 
     * @return Certificate information if available, null otherwise
     */
    fun getCertificateInfo(): CertificateUtils.CertificateInfo? {
        return certificateInfo
    }
    
    
    /**
     * Check if the server is currently running.
     * 
     * @return true if server is running, false otherwise
     */
    fun isRunning(): Boolean {
        return server != null
    }
}