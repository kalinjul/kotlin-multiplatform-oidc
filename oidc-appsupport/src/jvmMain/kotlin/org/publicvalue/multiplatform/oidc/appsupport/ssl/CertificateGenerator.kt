package org.publicvalue.multiplatform.oidc.appsupport.ssl

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import java.io.File
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.cert.X509Certificate

/**
 * Utility class for generating SSL certificates using various methods.
 * 
 * Provides certificate generation capabilities including:
 * - Self-signed certificates using keytool
 * - Simple certificate generation without external dependencies
 * - Certificate renewal and regeneration
 */
@ExperimentalOpenIdConnect
object CertificateGenerator {
    
    private const val CERTIFICATE_VALIDITY_DAYS = 365L
    private const val KEY_SIZE = 2048
    private const val KEYSTORE_TYPE = "PKCS12"
    
    /**
     * Generate a self-signed certificate for localhost using keytool.
     * 
     * This creates a certificate that browsers will show as "not secure" but will
     * still establish an encrypted connection. Uses keytool command for certificate generation.
     * 
     * @param validityDays How long the certificate should be valid (default 365 days)
     * @return CertificateInfo containing the certificate, private key, and keystore
     */
    fun generateLocalhostCertificate(validityDays: Long = CERTIFICATE_VALIDITY_DAYS): CertificateUtils.CertificateInfo {
        return try {
            val tempKeystoreFile = File.createTempFile("localhost", ".p12")
            tempKeystoreFile.deleteOnExit()
            tempKeystoreFile.delete()
            
            val password = "localhost"
            
            val keytoolCommand = listOf(
                "keytool",
                "-genkeypair",
                "-alias", "localhost",
                "-keyalg", "RSA",
                "-keysize", KEY_SIZE.toString(),
                "-storetype", KEYSTORE_TYPE,
                "-keystore", tempKeystoreFile.absolutePath,
                "-validity", validityDays.toString(),
                "-storepass", password,
                "-keypass", password,
                "-dname", "CN=localhost, OU=OIDC Client, O=Local Development, C=US",
                "-ext", "SAN=dns:localhost,dns:127.0.0.1,ip:127.0.0.1"
            )
            
            val processBuilder = ProcessBuilder(keytoolCommand)
                .redirectErrorStream(true)
                
            val env = processBuilder.environment()
            env["PATH"] = System.getenv("PATH") ?: ""
            env["JAVA_HOME"] = System.getenv("JAVA_HOME") ?: ""
            
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            
            if (exitCode != 0) {
                throw RuntimeException("keytool failed with exit code $exitCode: $output")
            }
            
            val keyStore = KeyStoreManager.loadKeyStore(tempKeystoreFile, password)
            val certificate = keyStore.getCertificate("localhost") as X509Certificate
            val privateKey = keyStore.getKey("localhost", password.toCharArray()) as java.security.PrivateKey
            
            tempKeystoreFile.delete()
            
            CertificateUtils.CertificateInfo(certificate, privateKey, keyStore)
            
        } catch (e: Exception) {
            generateSimpleLocalhostCertificate(validityDays)
        }
    }
    
    /**
     * Generate a simple self-signed certificate for localhost without external dependencies.
     * 
     * This is a fallback method when keytool is not available or fails.
     * 
     * @param validityDays How long the certificate should be valid
     * @return CertificateInfo containing the generated certificate
     */
    private fun generateSimpleLocalhostCertificate(validityDays: Long): CertificateUtils.CertificateInfo {
        return try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(KEY_SIZE, SecureRandom())
            val keyPair = keyPairGenerator.generateKeyPair()
            
            val certificate = createSelfSignedCertificate(keyPair, validityDays)
            val keyStore = KeyStoreManager.createKeyStore(certificate, keyPair.private)
            
            CertificateUtils.CertificateInfo(certificate, keyPair.private, keyStore)
            
        } catch (e: Exception) {
            throw RuntimeException("Failed to generate localhost certificate", e)
        }
    }
    
    /**
     * Create a self-signed X.509 certificate using keytool command.
     * 
     * This is a simpler fallback that creates a basic certificate without SAN extensions.
     * 
     * @param keyPair The key pair to use for the certificate
     * @param validityDays How long the certificate should be valid
     * @return X509Certificate instance
     */
    private fun createSelfSignedCertificate(keyPair: KeyPair, validityDays: Long): X509Certificate {
        val tempKeystoreFile = File.createTempFile("simple-cert", ".p12")
        tempKeystoreFile.deleteOnExit()
        tempKeystoreFile.delete()
        
        val password = "temppass"
        
        try {
            val keytoolCommand = listOf(
                "keytool",
                "-genkeypair",
                "-alias", "temp",
                "-keyalg", "RSA",
                "-keysize", KEY_SIZE.toString(),
                "-storetype", KEYSTORE_TYPE,
                "-keystore", tempKeystoreFile.absolutePath,
                "-validity", validityDays.toString(),
                "-storepass", password,
                "-keypass", password,
                "-dname", "CN=localhost"
            )
            
            val processBuilder = ProcessBuilder(keytoolCommand)
                .redirectErrorStream(true)
                
            val env = processBuilder.environment()
            env["PATH"] = System.getenv("PATH") ?: ""
            env["JAVA_HOME"] = System.getenv("JAVA_HOME") ?: ""
            
            val process = processBuilder.start()
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            
            if (exitCode != 0) {
                throw RuntimeException("keytool fallback failed: exit code $exitCode, output: $output")
            }
            
            val keyStore = KeyStoreManager.loadKeyStore(tempKeystoreFile, password)
            val certificate = keyStore.getCertificate("temp") as X509Certificate
            
            tempKeystoreFile.delete()
            return certificate
            
        } catch (e: Exception) {
            tempKeystoreFile.delete()
            throw RuntimeException("Certificate generation failed. Please ensure 'keytool' is available in PATH or provide a pre-generated certificate.", e)
        }
    }
    
    /**
     * Try to find keytool executable in PATH.
     * 
     * @return Path to keytool if found, null otherwise
     */
    private fun findKeytoolInPath(): String? {
        val pathEnv = System.getenv("PATH") ?: return null
        val pathSeparator = if (System.getProperty("os.name").lowercase().contains("windows")) ";" else ":"
        val executableSuffix = if (System.getProperty("os.name").lowercase().contains("windows")) ".exe" else ""
        
        for (pathDir in pathEnv.split(pathSeparator)) {
            val keytoolFile = File(pathDir, "keytool$executableSuffix")
            if (keytoolFile.exists() && keytoolFile.canExecute()) {
                return keytoolFile.absolutePath
            }
        }
        return null
    }
    
    /**
     * Test if keytool is accessible by running a simple command.
     * 
     * @return true if keytool is accessible, false otherwise
     */
    fun testKeytoolAvailability(): Boolean {
        return try {
            val process = ProcessBuilder("keytool", "-help")
                .redirectErrorStream(true)
                .start()
            val exitCode = process.waitFor()
            exitCode == 0
        } catch (e: Exception) {
            false
        }
    }
}