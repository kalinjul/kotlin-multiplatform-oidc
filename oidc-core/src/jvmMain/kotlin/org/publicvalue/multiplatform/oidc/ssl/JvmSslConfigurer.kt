package org.publicvalue.multiplatform.oidc.ssl

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttpConfig
import okhttp3.OkHttpClient
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Configures SSL/TLS settings for Ktor OkHttp engine on JVM
 */
@ExperimentalOpenIdConnect
object JvmSslConfigurer {
    
    /**
     * Apply SSL configuration to Ktor HttpClient with OkHttp engine
     */
    fun HttpClientConfig<OkHttpConfig>.configureSsl(sslConfig: SslConfig) {
        engine {
            config {
                configureSslSocketFactory(sslConfig)
                configureHostnameVerifier(sslConfig)
            }
        }
    }
    
    /**
     * Configure SSL socket factory for OkHttpClient
     */
    private fun OkHttpClient.Builder.configureSslSocketFactory(sslConfig: SslConfig) {
        when {
            sslConfig.disableCertificateValidation -> {
                // WARNING: This disables all certificate validation
                val trustManager = createUnsafeTrustManager()
                val sslContext = SSLContext.getInstance("TLS").apply {
                    init(null, arrayOf(trustManager), SecureRandom())
                }
                sslSocketFactory(sslContext.socketFactory, trustManager)
            }
            
            sslConfig.customTrustManager != null -> {
                // Use custom trust manager
                val trustManager = sslConfig.customTrustManager as X509TrustManager
                val sslContext = createSslContext(sslConfig, arrayOf(trustManager))
                sslSocketFactory(sslContext.socketFactory, trustManager)
            }
            
            else -> {
                // Use trust store configuration
                val trustManagers = createTrustManagers(sslConfig)
                val x509TrustManager = trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager
                    ?: throw IllegalStateException("No X509TrustManager found")
                
                val sslContext = createSslContext(sslConfig, trustManagers)
                sslSocketFactory(sslContext.socketFactory, x509TrustManager)
            }
        }
    }
    
    /**
     * Configure hostname verifier for OkHttpClient
     */
    private fun OkHttpClient.Builder.configureHostnameVerifier(sslConfig: SslConfig) {
        when {
            sslConfig.disableHostnameVerification -> {
                // WARNING: This disables hostname verification
                hostnameVerifier(HostnameVerifier { _, _ -> true })
            }
            
            sslConfig.customHostnameVerifier != null -> {
                hostnameVerifier(sslConfig.customHostnameVerifier)
            }
            
            // Otherwise use default hostname verification
        }
    }
    
    /**
     * Create SSL context with the given configuration
     */
    private fun createSslContext(sslConfig: SslConfig, trustManagers: Array<TrustManager>): SSLContext {
        val keyManagers = sslConfig.keyStorePath?.let { createKeyManagers(sslConfig) }
        
        return SSLContext.getInstance("TLS").apply {
            init(keyManagers, trustManagers, SecureRandom())
        }
    }
    
    /**
     * Create trust managers from SSL configuration
     */
    private fun createTrustManagers(sslConfig: SslConfig): Array<TrustManager> {
        return when {
            sslConfig.trustStorePath != null -> {
                // Load custom trust store
                val trustStore = KeyStoreUtils.loadKeyStore(
                    sslConfig.trustStorePath,
                    sslConfig.trustStorePassword,
                    sslConfig.trustStoreType
                )
                
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(trustStore)
                
                val trustManagers = trustManagerFactory.trustManagers
                
                // If additional certificates are specified, wrap the trust manager
                if (sslConfig.additionalTrustedCertificates.isNotEmpty()) {
                    val defaultTrustManager = trustManagers.firstOrNull { it is X509TrustManager } as? X509TrustManager
                        ?: throw IllegalStateException("No X509TrustManager found")
                    
                    arrayOf(CompositeX509TrustManager(defaultTrustManager, sslConfig.additionalTrustedCertificates))
                } else {
                    trustManagers
                }
            }
            
            sslConfig.additionalTrustedCertificates.isNotEmpty() -> {
                // Use default trust store but add additional certificates
                val defaultTrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                defaultTrustManagerFactory.init(null as java.security.KeyStore?) // Use default
                
                val defaultTrustManager = defaultTrustManagerFactory.trustManagers
                    .firstOrNull { it is X509TrustManager } as? X509TrustManager
                    ?: throw IllegalStateException("No default X509TrustManager found")
                
                arrayOf(CompositeX509TrustManager(defaultTrustManager, sslConfig.additionalTrustedCertificates))
            }
            
            else -> {
                // Use default trust managers
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(null as java.security.KeyStore?) // Use default
                trustManagerFactory.trustManagers
            }
        }
    }
    
    /**
     * Create key managers for client certificate authentication
     */
    private fun createKeyManagers(sslConfig: SslConfig): Array<javax.net.ssl.KeyManager>? {
        return sslConfig.keyStorePath?.let { keyStorePath ->
            val keyStore = KeyStoreUtils.loadKeyStore(
                keyStorePath,
                sslConfig.keyStorePassword,
                sslConfig.keyStoreType
            )
            
            val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            keyManagerFactory.init(keyStore, sslConfig.keyStorePassword?.toCharArray())
            keyManagerFactory.keyManagers
        }
    }
    
    /**
     * Create an unsafe trust manager that accepts all certificates
     * WARNING: This is insecure and should only be used for testing!
     */
    private fun createUnsafeTrustManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                // Accept all certificates
            }
            
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                // Accept all certificates
            }
            
            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return emptyArray()
            }
        }
    }
}

/**
 * Composite trust manager that combines default trust manager with additional trusted certificates
 */
@ExperimentalOpenIdConnect
private class CompositeX509TrustManager(
    private val defaultTrustManager: X509TrustManager,
    private val additionalTrustedCertificates: List<X509Certificate>
) : X509TrustManager {
    
    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
        defaultTrustManager.checkClientTrusted(chain, authType)
    }
    
    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
        try {
            defaultTrustManager.checkServerTrusted(chain, authType)
        } catch (e: Exception) {
            validateChainWithAdditionalCertificates(chain, authType)
        }
    }
    
    /**
     * Validate certificate chain against additional trusted certificates.
     * This includes both direct certificate matches and CA certificate validation.
     */
    private fun validateChainWithAdditionalCertificates(chain: Array<X509Certificate>, authType: String) {
        if (chain.isEmpty()) {
            throw IllegalArgumentException("Certificate chain is empty")
        }
        
        val serverCert = chain[0] // The server's certificate is always first
        
        // Check if the server certificate is directly trusted
        val isDirectlyTrusted = additionalTrustedCertificates.any { trustedCert ->
            serverCert.encoded.contentEquals(trustedCert.encoded)
        }
        
        if (isDirectlyTrusted) {
            return // Server certificate is directly trusted
        }
        
        // Check if the server certificate or any intermediate was signed by our trusted CA certificates
        val isSignedByTrustedCA = validateCertificateChainWithCAs(chain)
        
        if (!isSignedByTrustedCA) {
            throw IllegalArgumentException("Certificate chain validation failed: no trusted CA found for certificate chain")
        }
    }
    
    /**
     * Validate that the certificate chain can be trusted using our additional CA certificates.
     */
    private fun validateCertificateChainWithCAs(chain: Array<X509Certificate>): Boolean {
        // Check each certificate in the chain to see if it was signed by one of our trusted CAs
        for (cert in chain) {
            for (trustedCA in additionalTrustedCertificates) {
                if (isCertificateSignedByCA(cert, trustedCA)) {
                    // Found a certificate in the chain that was signed by one of our trusted CAs
                    return true
                }
            }
        }
        
        // Also check if we can build a valid chain with the trusted CAs
        return buildAndValidateCertificateChain(chain)
    }
    
    /**
     * Check if a certificate was signed by a specific CA certificate.
     */
    private fun isCertificateSignedByCA(cert: X509Certificate, caCert: X509Certificate): Boolean {
        return try {
            if (cert.issuerX500Principal.equals(caCert.subjectX500Principal)) {
                cert.verify(caCert.publicKey)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Attempt to build and validate a complete certificate chain using trusted CAs.
     */
    private fun buildAndValidateCertificateChain(chain: Array<X509Certificate>): Boolean {
        if (chain.isEmpty()) return false
        
        val serverCert = chain[0]
        
        // Try to find a path from the server certificate to one of our trusted CA certificates
        return findPathToTrustedCA(serverCert, chain.toSet(), mutableSetOf())
    }
    
    /**
     * Recursively find a path from the given certificate to a trusted CA.
     */
    private fun findPathToTrustedCA(
        currentCert: X509Certificate, 
        availableCerts: Set<X509Certificate>, 
        visitedCerts: MutableSet<X509Certificate>
    ): Boolean {
        // Avoid infinite loops
        if (currentCert in visitedCerts) {
            return false
        }
        visitedCerts.add(currentCert)
        
        // Check if current certificate was signed by any of our trusted CAs
        for (trustedCA in additionalTrustedCertificates) {
            if (isCertificateSignedByCA(currentCert, trustedCA)) {
                return true
            }
        }
        
        // Look for an issuer certificate in the available certificates
        val issuerCert = availableCerts.find { cert ->
            currentCert.issuerX500Principal.equals(cert.subjectX500Principal) && cert != currentCert
        }
        
        return if (issuerCert != null) {
            // Continue up the chain
            findPathToTrustedCA(issuerCert, availableCerts, visitedCerts)
        } else {
            false
        }
    }
    
    override fun getAcceptedIssuers(): Array<X509Certificate> {
        val defaultIssuers = defaultTrustManager.acceptedIssuers
        return defaultIssuers + additionalTrustedCertificates.toTypedArray()
    }
}