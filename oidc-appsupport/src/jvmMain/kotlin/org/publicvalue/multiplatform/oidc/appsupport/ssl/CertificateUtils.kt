package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Core data structures for SSL certificate management.
 */
@ExperimentalOpenIdConnect
object CertificateUtils {
    
    /**
     * Represents a certificate with its private key and keystore.
     */
    data class CertificateInfo(
        val certificate: X509Certificate,
        val privateKey: PrivateKey,
        val keyStore: KeyStore
    )
}