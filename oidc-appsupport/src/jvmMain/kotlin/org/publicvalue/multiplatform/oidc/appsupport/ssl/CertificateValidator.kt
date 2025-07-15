package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.security.cert.X509Certificate
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * Utility class for validating SSL certificates.
 * 
 * Provides functionality for:
 * - Certificate expiration checking
 * - Certificate validity verification
 * - Certificate chain validation
 * - Certificate metadata extraction
 */
@ExperimentalOpenIdConnect
object CertificateValidator {
    
    /**
     * Check if a certificate is still valid.
     * 
     * Validates that the certificate is not expired and won't expire within the buffer period.
     * 
     * @param certificate The certificate to check
     * @param bufferDays Number of days before expiry to consider invalid (default 30)
     * @return true if certificate is valid, false otherwise
     */
    fun isCertificateValid(certificate: X509Certificate, bufferDays: Long = 30): Boolean {
        return try {
            val now = Date()
            val bufferTime = Date.from(Instant.now().plus(bufferDays, ChronoUnit.DAYS))
            
            now.after(certificate.notBefore) && bufferTime.before(certificate.notAfter)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if a certificate is currently valid (not considering buffer period).
     * 
     * @param certificate The certificate to check
     * @return true if certificate is currently valid, false otherwise
     */
    fun isCertificateCurrentlyValid(certificate: X509Certificate): Boolean {
        return try {
            val now = Date()
            now.after(certificate.notBefore) && now.before(certificate.notAfter)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the number of days until certificate expiration.
     * 
     * @param certificate The certificate to check
     * @return Number of days until expiration, negative if already expired
     */
    fun getDaysUntilExpiration(certificate: X509Certificate): Long {
        return try {
            val now = Instant.now()
            val expiration = certificate.notAfter.toInstant()
            ChronoUnit.DAYS.between(now, expiration)
        } catch (e: Exception) {
            -1L
        }
    }
    
    /**
     * Check if a certificate needs renewal based on the buffer period.
     * 
     * @param certificate The certificate to check
     * @param renewalBufferDays Days before expiration to consider renewal needed
     * @return true if certificate needs renewal, false otherwise
     */
    fun needsRenewal(certificate: X509Certificate, renewalBufferDays: Long = 30): Boolean {
        return try {
            val daysUntilExpiration = getDaysUntilExpiration(certificate)
            daysUntilExpiration <= renewalBufferDays
        } catch (e: Exception) {
            true
        }
    }
    
    /**
     * Validate certificate for a specific hostname.
     * 
     * Basic validation that checks if the certificate subject matches the hostname.
     * Note: This is a simplified validation and doesn't perform full certificate chain validation.
     * 
     * @param certificate The certificate to validate
     * @param hostname The hostname to validate against
     * @return true if certificate appears valid for the hostname
     */
    fun validateForHostname(certificate: X509Certificate, hostname: String): Boolean {
        return try {
            val subjectDN = certificate.subjectX500Principal.name
            
            subjectDN.contains("CN=$hostname") || 
            subjectDN.contains("CN=*.${hostname.substringAfter(".")}")
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Extract certificate information as a readable string.
     * 
     * @param certificate The certificate to extract information from
     * @return Formatted string containing certificate details
     */
    fun getCertificateInfo(certificate: X509Certificate): String {
        return try {
            buildString {
                appendLine("Subject: ${certificate.subjectX500Principal.name}")
                appendLine("Issuer: ${certificate.issuerX500Principal.name}")
                appendLine("Serial Number: ${certificate.serialNumber}")
                appendLine("Valid From: ${certificate.notBefore}")
                appendLine("Valid Until: ${certificate.notAfter}")
                appendLine("Signature Algorithm: ${certificate.sigAlgName}")
                appendLine("Currently Valid: ${isCertificateCurrentlyValid(certificate)}")
                appendLine("Days Until Expiration: ${getDaysUntilExpiration(certificate)}")
                appendLine("Needs Renewal: ${needsRenewal(certificate)}")
            }
        } catch (e: Exception) {
            "Error extracting certificate information: ${e.message}"
        }
    }
    
    /**
     * Check if a certificate is self-signed.
     * 
     * @param certificate The certificate to check
     * @return true if certificate appears to be self-signed
     */
    fun isSelfSigned(certificate: X509Certificate): Boolean {
        return try {
            certificate.issuerX500Principal.equals(certificate.subjectX500Principal)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the certificate's key usage extensions.
     * 
     * @param certificate The certificate to check
     * @return List of key usage strings, empty if none found
     */
    fun getKeyUsage(certificate: X509Certificate): List<String> {
        return try {
            val keyUsage = certificate.keyUsage
            if (keyUsage != null) {
                val usages = mutableListOf<String>()
                val keyUsageNames = arrayOf(
                    "Digital Signature", "Non Repudiation", "Key Encipherment",
                    "Data Encipherment", "Key Agreement", "Key Cert Sign",
                    "CRL Sign", "Encipher Only", "Decipher Only"
                )
                
                for (i in keyUsage.indices) {
                    if (keyUsage[i] && i < keyUsageNames.size) {
                        usages.add(keyUsageNames[i])
                    }
                }
                usages
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Validate certificate chain (basic validation).
     * 
     * @param certificates Array of certificates forming the chain
     * @return true if the chain appears valid, false otherwise
     */
    fun validateCertificateChain(certificates: Array<X509Certificate>): Boolean {
        return try {
            if (certificates.isEmpty()) return false
            
            // Basic chain validation - each certificate should be signed by the next
            for (i in 0 until certificates.size - 1) {
                val current = certificates[i]
                val issuer = certificates[i + 1]
                
                // Verify that the issuer DN matches the subject DN of the next certificate
                if (!current.issuerX500Principal.equals(issuer.subjectX500Principal)) {
                    return false
                }
            }
            
            true
        } catch (e: Exception) {
            false
        }
    }
}