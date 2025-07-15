package presentation

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSourceFactory
import org.publicvalue.multiplatform.oidc.appsupport.ssl.ResourcesCertificateSource
import presentation.state.DebugLogger
import java.io.File
import java.security.MessageDigest
import java.security.cert.X509Certificate

data class CertificateInfo(
    val source: CertificateSource,
    val location: String,
    val description: String,
    val available: Boolean
)

enum class CertificateSource {
    DOCKER,
    RESOURCES,
    SELF_SIGNED,
    LETS_ENCRYPT
}

@OptIn(ExperimentalOpenIdConnect::class)
class CertificateManager(private val debugLogger: DebugLogger) {
    
    private var _certificateInfo: List<CertificateInfo> = emptyList()
    val certificateInfo: List<CertificateInfo> get() = _certificateInfo
    
    init {
        refreshCertificateInfo()
    }
    
    fun refreshCertificateInfo() {
        debugLogger.logInfo("Refreshing certificate availability information...")
        _certificateInfo = listOf(
            checkDockerCertificate(),
            checkResourcesCertificate(),
            checkSelfSignedCertificate(),
            checkLetsEncryptCertificate()
        )
    }
    
    private fun checkDockerCertificate(): CertificateInfo {
        val dockerCertFile = File("docker/certs/localhost/localhost.p12")
        val available = dockerCertFile.exists() && dockerCertFile.canRead()
        
        return CertificateInfo(
            source = CertificateSource.DOCKER,
            location = dockerCertFile.absolutePath,
            description = "Docker-generated certificate (localhost.p12)",
            available = available
        )
    }
    
    private fun checkResourcesCertificate(): CertificateInfo {
        val available = try {
            val resourcesSource = CertificateSourceFactory.fromResources()
            (resourcesSource as? ResourcesCertificateSource)?.hasResourceCertificate("localhost") ?: false
        } catch (e: Exception) {
            debugLogger.logError("Failed to check resources certificate", e)
            false
        }
        
        return CertificateInfo(
            source = CertificateSource.RESOURCES,
            location = "Application resources (/ssl/certificates/)",
            description = "Resources certificate (bundled with application)",
            available = available
        )
    }
    
    private fun checkSelfSignedCertificate(): CertificateInfo {
        return CertificateInfo(
            source = CertificateSource.SELF_SIGNED,
            location = "Auto-generated in memory",
            description = "Auto-generated self-signed certificate",
            available = true // Always available as fallback
        )
    }
    
    private fun checkLetsEncryptCertificate(): CertificateInfo {
        val letsEncryptCertFile = File(System.getProperty("user.home"), ".letsencrypt/localhost.p12")
        val available = letsEncryptCertFile.exists() && letsEncryptCertFile.canRead()
        
        return CertificateInfo(
            source = CertificateSource.LETS_ENCRYPT,
            location = letsEncryptCertFile.absolutePath,
            description = "Let's Encrypt certificate (automatic)",
            available = available
        )
    }
    
    fun getPreferredCertificate(): CertificateInfo? {
        return _certificateInfo.firstOrNull { it.available }
    }
    
    fun getCertificateBySource(source: CertificateSource): CertificateInfo? {
        return _certificateInfo.find { it.source == source }
    }
    
    fun createCertificateSource(source: CertificateSource): org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSource? {
        return when (source) {
            CertificateSource.DOCKER -> {
                val dockerCertFile = File("docker/certs/localhost/localhost.p12")
                if (dockerCertFile.exists() && dockerCertFile.canRead()) {
                    CertificateSourceFactory.fromFile(dockerCertFile, "localhost")
                } else null
            }
            CertificateSource.RESOURCES -> {
                try {
                    CertificateSourceFactory.fromResources()
                } catch (e: Exception) {
                    debugLogger.logError("Failed to create resources certificate source", e)
                    null
                }
            }
            CertificateSource.SELF_SIGNED -> {
                CertificateSourceFactory.selfSigned()
            }
            CertificateSource.LETS_ENCRYPT -> {
                val letsEncryptCertFile = File(System.getProperty("user.home"), ".letsencrypt/localhost.p12")
                if (letsEncryptCertFile.exists() && letsEncryptCertFile.canRead()) {
                    CertificateSourceFactory.fromFile(letsEncryptCertFile, "letsencrypt")
                } else null
            }
        }
    }
    
    fun getCertificateFingerprint(cert: X509Certificate): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val fingerprint = digest.digest(cert.encoded)
            fingerprint.joinToString(":") { "%02x".format(it) }
        } catch (e: Exception) {
            "Unable to calculate fingerprint: ${e.message}"
        }
    }
    
    fun logCertificateEnvironment() {
        debugLogger.logInfo("=== Certificate Environment Analysis ===")
        debugLogger.logInfo("Working directory: ${System.getProperty("user.dir")}")
        debugLogger.logInfo("User home: ${System.getProperty("user.home")}")
        
        _certificateInfo.forEach { cert ->
            debugLogger.logInfo("${cert.source.name} certificate:")
            debugLogger.logInfo("  - Location: ${cert.location}")
            debugLogger.logInfo("  - Available: ${cert.available}")
            debugLogger.logInfo("  - Description: ${cert.description}")
        }
    }
}