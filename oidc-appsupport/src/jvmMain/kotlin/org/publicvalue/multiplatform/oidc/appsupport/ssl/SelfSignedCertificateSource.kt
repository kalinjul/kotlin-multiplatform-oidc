package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File

/**
 * Certificate source that generates self-signed certificates.
 * 
 * This is the default certificate source for development environments.
 * Generated certificates will show as "untrusted" in browsers but provide encryption.
 * 
 * @param validityDays How long the certificate should be valid (default 365 days)
 * @param forceRegenerate Whether to force regeneration of existing certificates
 * @param certificateFile Optional file to store the certificate (uses temporary file if null)
 * @param password Password for the certificate keystore
 */
@ExperimentalOpenIdConnect
class SelfSignedCertificateSource(
    private val validityDays: Long = 365L,
    private val forceRegenerate: Boolean = false,
    private val certificateFile: File? = null,
    private val password: String = "localhost"
) : CertificateSource {
    
    override val supportsAutoRenewal = false
    override val displayName = "Self-Signed Certificate"
    
    /**
     * Generate or retrieve a self-signed certificate for the specified hostname.
     * 
     * @param hostname The hostname the certificate should be valid for
     * @return CertificateInfo containing the generated certificate and private key
     */
    override suspend fun getCertificate(hostname: String): CertificateUtils.CertificateInfo {
        return if (certificateFile != null) {
            KeyStoreManager.getOrCreateLocalhostCertificate(
                keystoreFile = certificateFile,
                password = password,
                forceRegenerate = forceRegenerate
            )
        } else {
            CertificateGenerator.generateLocalhostCertificate(validityDays)
        }
    }
}