package org.publicvalue.multiplatform.oidc.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * SSL/TLS configuration for JVM HTTP clients.
 * Allows customization of trust stores, key stores, and certificate validation.
 */
@ExperimentalOpenIdConnect
data class SslConfig(
    /**
     * Path to custom trust store file (JKS, PKCS12, etc.)
     */
    val trustStorePath: String? = null,
    
    /**
     * Password for the trust store
     */
    val trustStorePassword: String? = null,
    
    /**
     * Trust store type (JKS, PKCS12, etc.). If null, will be inferred from file extension.
     */
    val trustStoreType: String? = null,
    
    /**
     * Path to custom key store file for client certificate authentication
     */
    val keyStorePath: String? = null,
    
    /**
     * Password for the key store
     */
    val keyStorePassword: String? = null,
    
    /**
     * Key store type (JKS, PKCS12, etc.). If null, will be inferred from file extension.
     */
    val keyStoreType: String? = null,
    
    /**
     * Custom trust manager for certificate validation.
     * If provided, overrides trust store configuration.
     */
    val customTrustManager: TrustManager? = null,
    
    /**
     * Custom hostname verifier.
     * If null, uses default hostname verification.
     */
    val customHostnameVerifier: HostnameVerifier? = null,
    
    /**
     * Disable hostname verification entirely.
     * WARNING: This is insecure and should only be used for testing.
     */
    val disableHostnameVerification: Boolean = false,
    
    /**
     * Disable all certificate validation.
     * WARNING: This is extremely insecure and should only be used for testing.
     */
    val disableCertificateValidation: Boolean = false,
    
    /**
     * Additional trusted certificates to add to the default trust store
     */
    val additionalTrustedCertificates: List<X509Certificate> = emptyList()
) {
    
    companion object {
        /**
         * Create SSL configuration that disables all validation.
         * WARNING: Only use for testing/development!
         */
        fun unsafe(): SslConfig = SslConfig(
            disableHostnameVerification = true,
            disableCertificateValidation = true
        )
        
        /**
         * Create SSL configuration with custom trust store from file path
         */
        fun withTrustStore(
            path: String, 
            password: String? = null, 
            type: String? = null
        ): SslConfig = SslConfig(
            trustStorePath = path,
            trustStorePassword = password,
            trustStoreType = type
        )
        
        /**
         * Create SSL configuration with client certificate authentication
         */
        fun withClientCertificate(
            keyStorePath: String,
            keyStorePassword: String? = null,
            keyStoreType: String? = null,
            trustStorePath: String? = null,
            trustStorePassword: String? = null,
            trustStoreType: String? = null
        ): SslConfig = SslConfig(
            keyStorePath = keyStorePath,
            keyStorePassword = keyStorePassword,
            keyStoreType = keyStoreType,
            trustStorePath = trustStorePath,
            trustStorePassword = trustStorePassword,
            trustStoreType = trustStoreType
        )
    }
}

/**
 * Builder for SSL configuration with DSL support
 */
@ExperimentalOpenIdConnect
class SslConfigBuilder {
    var trustStorePath: String? = null
    var trustStorePassword: String? = null
    var trustStoreType: String? = null
    var keyStorePath: String? = null
    var keyStorePassword: String? = null
    var keyStoreType: String? = null
    var customTrustManager: TrustManager? = null
    var customHostnameVerifier: HostnameVerifier? = null
    var disableHostnameVerification: Boolean = false
    var disableCertificateValidation: Boolean = false
    var additionalTrustedCertificates: MutableList<X509Certificate> = mutableListOf()
    
    /**
     * Configure trust store from file path
     */
    fun trustStore(path: String, password: String? = null, type: String? = null) {
        trustStorePath = path
        trustStorePassword = password
        trustStoreType = type
    }
    
    /**
     * Configure trust store from File
     */
    fun trustStore(file: File, password: String? = null, type: String? = null) {
        trustStore(file.absolutePath, password, type)
    }
    
    /**
     * Configure key store for client certificate authentication
     */
    fun keyStore(path: String, password: String? = null, type: String? = null) {
        keyStorePath = path
        keyStorePassword = password
        keyStoreType = type
    }
    
    /**
     * Configure key store from File
     */
    fun keyStore(file: File, password: String? = null, type: String? = null) {
        keyStore(file.absolutePath, password, type)
    }
    
    /**
     * Add a trusted certificate
     */
    fun addTrustedCertificate(certificate: X509Certificate) {
        additionalTrustedCertificates.add(certificate)
    }
    
    /**
     * Set custom trust manager
     */
    fun trustManager(trustManager: TrustManager) {
        customTrustManager = trustManager
    }
    
    /**
     * Set custom hostname verifier
     */
    fun hostnameVerifier(verifier: HostnameVerifier) {
        customHostnameVerifier = verifier
    }
    
    /**
     * Disable hostname verification (insecure)
     */
    fun disableHostnameVerification() {
        disableHostnameVerification = true
    }
    
    /**
     * Disable all certificate validation (very insecure)
     */
    fun disableCertificateValidation() {
        disableCertificateValidation = true
    }
    
    internal fun build(): SslConfig = SslConfig(
        trustStorePath = trustStorePath,
        trustStorePassword = trustStorePassword,
        trustStoreType = trustStoreType,
        keyStorePath = keyStorePath,
        keyStorePassword = keyStorePassword,
        keyStoreType = keyStoreType,
        customTrustManager = customTrustManager,
        customHostnameVerifier = customHostnameVerifier,
        disableHostnameVerification = disableHostnameVerification,
        disableCertificateValidation = disableCertificateValidation,
        additionalTrustedCertificates = additionalTrustedCertificates.toList()
    )
}

/**
 * Utility functions for working with KeyStores
 */
@ExperimentalOpenIdConnect
object KeyStoreUtils {
    
    /**
     * Load a KeyStore from file path
     */
    fun loadKeyStore(path: String, password: String?, type: String? = null): KeyStore {
        val file = File(path)
        require(file.exists()) { "KeyStore file not found: $path" }
        
        val storeType = type ?: inferKeyStoreType(path)
        val keyStore = KeyStore.getInstance(storeType)
        
        file.inputStream().use { inputStream ->
            keyStore.load(inputStream, password?.toCharArray())
        }
        
        return keyStore
    }
    
    /**
     * Load a KeyStore from InputStream
     */
    fun loadKeyStore(inputStream: InputStream, password: String?, type: String = "JKS"): KeyStore {
        val keyStore = KeyStore.getInstance(type)
        keyStore.load(inputStream, password?.toCharArray())
        return keyStore
    }
    
    /**
     * Infer KeyStore type from file extension
     */
    private fun inferKeyStoreType(path: String): String {
        return when (path.substringAfterLast('.', "").lowercase()) {
            "jks" -> "JKS"
            "p12", "pfx" -> "PKCS12"
            "bks" -> "BKS"
            else -> "JKS" // Default fallback
        }
    }
}