package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

/**
 * Interface for certificate providers that can supply SSL certificates for HTTPS servers.
 * 
 * This allows pluggable certificate sources including:
 * - Self-signed certificates (development)
 * - File-based certificates (production)
 * - Resource-based certificates (bundled)
 * - Key management services (enterprise)
 */
@ExperimentalOpenIdConnect
interface CertificateSource {
    
    /**
     * Get or create a certificate for the specified hostname.
     * 
     * @param hostname The hostname the certificate should be valid for
     * @return CertificateInfo containing certificate, private key, and keystore
     * @throws Exception if certificate cannot be obtained
     */
    suspend fun getCertificate(hostname: String = "localhost"): CertificateUtils.CertificateInfo
    
    /**
     * Check if this certificate source supports automatic renewal.
     */
    val supportsAutoRenewal: Boolean
    
    /**
     * Get the display name of this certificate source for logging/debugging.
     */
    val displayName: String
}

