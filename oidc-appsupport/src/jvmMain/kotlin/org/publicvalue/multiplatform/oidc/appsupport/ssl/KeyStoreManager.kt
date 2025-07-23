package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File
import java.io.FileOutputStream
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

/**
 * Utility class for managing KeyStore operations.
 * 
 * Provides functionality for:
 * - Creating and configuring KeyStore instances
 * - Loading and saving KeyStore files
 * - Managing certificates and keys within KeyStores
 * - Retrieving certificate information from KeyStores
 */
@ExperimentalOpenIdConnect
object KeyStoreManager {
    
    private const val KEYSTORE_TYPE = "PKCS12"
    
    /**
     * Create a PKCS12 keystore containing the certificate and private key.
     * 
     * @param certificate The X.509 certificate to store
     * @param privateKey The private key corresponding to the certificate
     * @param alias The alias to use in the keystore (default "localhost")
     * @param password The keystore password (default "localhost")
     * @return Configured KeyStore instance
     */
    fun createKeyStore(
        certificate: X509Certificate,
        privateKey: PrivateKey,
        alias: String = "localhost",
        password: String = "localhost"
    ): KeyStore {
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        keyStore.load(null, null)
        
        keyStore.setKeyEntry(
            alias,
            privateKey,
            password.toCharArray(),
            arrayOf(certificate)
        )
        
        return keyStore
    }
    
    /**
     * Save a keystore to a file.
     * 
     * Creates parent directories if they don't exist.
     * 
     * @param keyStore The keystore to save
     * @param file The file to save to
     * @param password The keystore password
     */
    fun saveKeyStore(keyStore: KeyStore, file: File, password: String = "localhost") {
        file.parentFile?.mkdirs()
        FileOutputStream(file).use { fos ->
            keyStore.store(fos, password.toCharArray())
        }
    }
    
    /**
     * Load a keystore from a file.
     * 
     * @param file The keystore file to load
     * @param password The keystore password
     * @return The loaded KeyStore instance
     * @throws IllegalArgumentException if the keystore file doesn't exist
     */
    fun loadKeyStore(file: File, password: String = "localhost"): KeyStore {
        require(file.exists()) { "Keystore file does not exist: ${file.absolutePath}" }
        
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE)
        file.inputStream().use { fis ->
            keyStore.load(fis, password.toCharArray())
        }
        return keyStore
    }
    
    /**
     * Get certificate information from an existing keystore file.
     * 
     * @param file The keystore file to read
     * @param alias The certificate alias to look for (default "localhost")
     * @param password The keystore password (default "localhost")
     * @return CertificateInfo if certificate exists and is valid, null otherwise
     */
    fun getCertificateFromKeyStore(
        file: File,
        alias: String = "localhost",
        password: String = "localhost"
    ): CertificateUtils.CertificateInfo? {
        return try {
            val keyStore = loadKeyStore(file, password)
            val certificate = keyStore.getCertificate(alias) as? X509Certificate
            val privateKey = keyStore.getKey(alias, password.toCharArray()) as? PrivateKey
            
            if (certificate != null && privateKey != null) {
                CertificateUtils.CertificateInfo(certificate, privateKey, keyStore)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Get or create a localhost certificate from the specified keystore file.
     * 
     * This method will:
     * 1. Check if a valid certificate exists in the specified file
     * 2. If valid certificate exists, return it
     * 3. If no valid certificate exists, generate a new one and save it
     * 
     * @param keystoreFile File to store/load the certificate
     * @param password Keystore password (default "localhost")
     * @param forceRegenerate Force generation of new certificate even if valid one exists
     * @return CertificateInfo for the localhost certificate
     */
    fun getOrCreateLocalhostCertificate(
        keystoreFile: File,
        password: String = "localhost",
        forceRegenerate: Boolean = false
    ): CertificateUtils.CertificateInfo {
        if (!forceRegenerate && keystoreFile.exists()) {
            getCertificateFromKeyStore(keystoreFile, password = password)?.let { certInfo ->
                if (CertificateValidator.isCertificateValid(certInfo.certificate)) {
                    return certInfo
                }
            }
        }
        
        val certInfo = CertificateGenerator.generateLocalhostCertificate()
        saveKeyStore(certInfo.keyStore, keystoreFile, password)
        
        return certInfo
    }
    
    /**
     * List all aliases in a keystore file.
     * 
     * @param file The keystore file to inspect
     * @param password The keystore password
     * @return List of aliases in the keystore
     */
    fun listAliases(file: File, password: String = "localhost"): List<String> {
        return try {
            val keyStore = loadKeyStore(file, password)
            keyStore.aliases().toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Check if a keystore contains a certificate with the specified alias.
     * 
     * @param file The keystore file to check
     * @param alias The alias to look for
     * @param password The keystore password
     * @return true if the alias exists and contains a valid certificate
     */
    fun containsCertificate(
        file: File,
        alias: String,
        password: String = "localhost"
    ): Boolean {
        return try {
            val keyStore = loadKeyStore(file, password)
            keyStore.containsAlias(alias) && keyStore.getCertificate(alias) != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get the default certificate storage location.
     * 
     * @return File pointing to default certificate location in user's home directory
     */
    fun getDefaultCertificateFile(): File {
        val userHome = System.getProperty("user.home")
        return File(userHome, ".oidc-client/localhost-cert.p12")
    }
}