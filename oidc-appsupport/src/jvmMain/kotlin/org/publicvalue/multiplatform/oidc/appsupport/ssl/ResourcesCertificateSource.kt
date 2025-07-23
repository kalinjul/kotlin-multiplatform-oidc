package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File
import java.security.KeyStore

/**
 * Certificate source that loads certificates from classpath resources.
 * 
 * This allows certificates to be bundled with the application in the resources folder,
 * providing an easy way to distribute pre-configured certificates without requiring
 * external certificate management.
 * 
 * Supports multiple certificate formats:
 * - PKCS12 (.p12, .pfx)
 * - JKS (.jks)
 * - PEM (.pem, .crt) - Future enhancement
 */
@ExperimentalOpenIdConnect
class ResourcesCertificateSource(
    private val resourcePath: String? = null,
    private val password: String = "localhost",
    private val alias: String = "localhost",
    private val validityBufferDays: Long = 30
) : CertificateSource {
    
    override val supportsAutoRenewal = false
    override val displayName = "Resources Certificate (${resourcePath ?: "auto-detected"})"
    
    companion object {
        private val DEFAULT_RESOURCE_PATHS = listOf(
            "/ssl/certificates/{hostname}.p12",
            "/ssl/certificates/{hostname}.jks", 
            "/ssl/certificates/localhost.p12",
            "/ssl/certificates/localhost.jks"
        )
    }
    
    override suspend fun getCertificate(hostname: String): CertificateUtils.CertificateInfo {
        val resourcePath = this.resourcePath ?: findCertificateResource(hostname)
            ?: throw IllegalStateException("No certificate found in resources for hostname: $hostname")
        
        val resourceStream = javaClass.getResourceAsStream(resourcePath)
            ?: throw IllegalStateException("Certificate resource not found: $resourcePath")
        
        val certInfo = try {
            // Create temporary file from resource stream
            val tempFile = File.createTempFile("resource-cert", ".tmp")
            tempFile.deleteOnExit()
            
            resourceStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            
            // Load certificate from temporary file
            KeyStoreManager.getCertificateFromKeyStore(
                file = tempFile,
                alias = alias,
                password = password
            ) ?: throw IllegalStateException("Could not load certificate from resource: $resourcePath")
        } catch (e: Exception) {
            throw IllegalStateException("Failed to load certificate from resource: $resourcePath", e)
        }
        
        // Check certificate validity
        if (!CertificateValidator.isCertificateValid(certInfo.certificate, validityBufferDays)) {
            throw IllegalStateException(
                "Certificate in resource $resourcePath is expired or expires within $validityBufferDays days"
            )
        }
        
        return certInfo
    }
    
    /**
     * Find the first available certificate resource for the given hostname.
     */
    private fun findCertificateResource(hostname: String): String? {
        return DEFAULT_RESOURCE_PATHS
            .map { it.replace("{hostname}", hostname) }
            .firstOrNull { resourcePath ->
                javaClass.getResourceAsStream(resourcePath) != null
            }
    }
    
    /**
     * Check if a certificate resource exists for the given hostname.
     */
    fun hasResourceCertificate(hostname: String = "localhost"): Boolean {
        return if (resourcePath != null) {
            javaClass.getResourceAsStream(resourcePath) != null
        } else {
            findCertificateResource(hostname) != null
        }
    }
    
    /**
     * List all available certificate resources.
     */
    fun listAvailableResources(): List<String> {
        return DEFAULT_RESOURCE_PATHS.filter { path ->
            // Check both hostname variants and literal paths
            javaClass.getResourceAsStream(path) != null ||
            javaClass.getResourceAsStream(path.replace("{hostname}", "localhost")) != null
        }
    }
}