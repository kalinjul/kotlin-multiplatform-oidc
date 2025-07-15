package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File

/**
 * Factory for creating certificate sources based on configuration.
 * 
 * Provides convenient factory methods for different certificate sources and automatic
 * detection of the best available certificate source based on the environment.
 */
@ExperimentalOpenIdConnect
object CertificateSourceFactory {
    
    /**
     * Create a self-signed certificate source (default behavior).
     * 
     * @param validityDays How long the certificate should be valid (default 365 days)
     * @param certificateFile Optional file to store the certificate
     * @param password Password for the certificate keystore
     * @return SelfSignedCertificateSource instance
     */
    fun selfSigned(
        validityDays: Long = 365L,
        certificateFile: File? = null,
        password: String = "localhost"
    ): CertificateSource = SelfSignedCertificateSource(
        validityDays = validityDays,
        certificateFile = certificateFile,
        password = password
    )
    
    /**
     * Create a file-based certificate source.
     * 
     * @param certificateFile The file containing the certificate
     * @param password Password for the certificate file
     * @param alias The certificate alias within the keystore
     * @return FileCertificateSource instance
     */
    fun fromFile(
        certificateFile: File,
        password: String,
        alias: String = "localhost"
    ): CertificateSource = FileCertificateSource(
        certificateFile = certificateFile,
        password = password,
        alias = alias
    )
    
    
    /**
     * Create a resources-based certificate source.
     * 
     * @param resourcePath Path to the certificate resource (optional)
     * @param password Password for the certificate file
     * @param alias The certificate alias within the keystore
     * @return ResourcesCertificateSource instance
     */
    fun fromResources(
        resourcePath: String? = null,
        password: String = "localhost",
        alias: String = "localhost"
    ): CertificateSource = ResourcesCertificateSource(
        resourcePath = resourcePath,
        password = password,
        alias = alias
    )
    
    /**
     * Auto-detect the best certificate source based on available files and configuration.
     * 
     * Priority order:
     * 1. Explicit certificate file parameter
     * 2. User-copied Docker certificates  
     * 3. Docker-generated certificates
     * 4. Common certificate locations
     * 5. Resources folder certificates
     * 6. Auto-generated self-signed certificates
     * 
     * @param hostname The hostname to search certificates for
     * @param certificateFile Optional explicit certificate file
     * @param password Password for certificate files
     * @return The best available CertificateSource
     */
    fun autoDetect(
        hostname: String = "localhost",
        certificateFile: File? = null,
        password: String = "localhost"
    ): CertificateSource {
        certificateFile?.let { file ->
            if (file.exists()) {
                return fromFile(file, password)
            }
        }
        
        val userDockerFile = File(System.getProperty("user.home"), ".oidc-desktop-ssl/$hostname.p12")
        if (userDockerFile.exists()) {
            return fromFile(userDockerFile, password)
        }
        
        val dockerFile = File("docker/certs/$hostname/$hostname.p12")
        if (dockerFile.exists()) {
            return fromFile(dockerFile, password)
        }
        
        val commonLocations = listOf(
            File("/etc/ssl/certs/$hostname.p12"),
            File("/etc/ssl/certs/$hostname.jks"),
            File(System.getProperty("user.home"), ".ssl/$hostname.p12"),
            File("./ssl/$hostname.p12"),
            File("./$hostname.p12")
        )
        
        for (location in commonLocations) {
            if (location.exists()) {
                return fromFile(location, password)
            }
        }
        
        val resourcesSource = fromResources(password = password)
        if ((resourcesSource as ResourcesCertificateSource).hasResourceCertificate(hostname)) {
            return resourcesSource
        }
        
        return selfSigned(certificateFile = certificateFile, password = password)
    }
}