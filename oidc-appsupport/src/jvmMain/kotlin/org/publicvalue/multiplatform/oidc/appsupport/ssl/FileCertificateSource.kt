package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File

/**
 * Certificate source that loads certificates from files.
 * 
 * Supports various certificate formats including PEM, PKCS12, and JKS.
 * Validates certificate expiration and provides clear error messages.
 * 
 * @param certificateFile The file containing the certificate
 * @param password Password for the certificate file
 * @param alias The certificate alias within the keystore
 * @param validityBufferDays Days before expiration to consider certificate invalid
 */
@ExperimentalOpenIdConnect
class FileCertificateSource(
    private val certificateFile: File,
    private val password: String,
    private val alias: String = "localhost",
    private val validityBufferDays: Long = 30
) : CertificateSource {
    
    override val supportsAutoRenewal = false
    override val displayName = "File-Based Certificate (${certificateFile.name})"
    
    /**
     * Load and validate a certificate from the configured file.
     * 
     * @param hostname The hostname to validate against (currently unused)
     * @return CertificateInfo containing the loaded certificate and private key
     * @throws IllegalStateException if certificate file doesn't exist or is invalid
     */
    override suspend fun getCertificate(hostname: String): CertificateUtils.CertificateInfo {
        require(certificateFile.exists()) { 
            "Certificate file does not exist: ${certificateFile.absolutePath}" 
        }
        
        val certInfo = KeyStoreManager.getCertificateFromKeyStore(
            file = certificateFile,
            alias = alias,
            password = password
        ) ?: throw IllegalStateException("Could not load certificate from ${certificateFile.absolutePath}")
        
        if (!CertificateValidator.isCertificateValid(certInfo.certificate, validityBufferDays)) {
            throw IllegalStateException(
                "Certificate in ${certificateFile.absolutePath} is expired or expires within $validityBufferDays days"
            )
        }
        
        return certInfo
    }
}