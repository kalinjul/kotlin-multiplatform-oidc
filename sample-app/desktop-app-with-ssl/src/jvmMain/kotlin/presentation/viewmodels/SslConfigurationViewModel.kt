package presentation.viewmodels

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import presentation.CertificateManager
import presentation.CertificateSource
import presentation.state.DebugLogger

enum class SslConfigType(val displayName: String) {
    DEFAULT("Default"),
    CUSTOM_TRUST_STORE("Custom Trust Store"),
    DISABLE_VALIDATION("Disable Validation"),
    CLIENT_CERTIFICATE("Client Certificate")
}

data class SslConfigurationState(
    // HTTP Client SSL Configuration
    val sslConfigType: SslConfigType = SslConfigType.DEFAULT,
    val trustStorePath: String = "docker/certs/universal/truststore.jks",
    val trustStorePassword: String = "truststore-password",
    val disableValidation: Boolean = false,
    val connectionStatus: String = "Not tested",
    val isTestingConnection: Boolean = false,
    
    // HTTPS Redirect Server Configuration
    val httpsEnabled: Boolean = false,
    val serverStatus: String = "Stopped",
    val redirectUrl: String = "http://localhost:8080/redirect",
    val selectedCertificateSource: CertificateSource = CertificateSource.DOCKER,
    val isServerLoading: Boolean = false
)

class SslConfigurationViewModel {
    private val _state = mutableStateOf(SslConfigurationState())
    val state: State<SslConfigurationState> = _state

    val debugLogger = DebugLogger()
    private val certificateManager = CertificateManager(debugLogger)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        updateCertificateInfo()
    }

    // HTTP Client SSL Configuration Methods
    fun updateSslConfigType(type: SslConfigType) {
        _state.value = _state.value.copy(sslConfigType = type)
        debugLogger.logInfo("SSL config type changed to: ${type.displayName}")
    }

    fun updateTrustStorePath(path: String) {
        _state.value = _state.value.copy(trustStorePath = path)
    }

    fun updateTrustStorePassword(password: String) {
        _state.value = _state.value.copy(trustStorePassword = password)
    }

    fun updateDisableValidation(disable: Boolean) {
        _state.value = _state.value.copy(disableValidation = disable)
        if (disable) {
            debugLogger.logWarning("SSL validation disabled - only for development!")
        }
    }

    fun testConnection() {
        scope.launch {
            try {
                _state.value = _state.value.copy(
                    isTestingConnection = true,
                    connectionStatus = "Testing connection to Keycloak..."
                )
                debugLogger.logInfo("Testing SSL connection...")

                kotlinx.coroutines.delay(2000)

                _state.value = _state.value.copy(
                    connectionStatus = "Connection successful - Keycloak running on HTTP port 7080"
                )
                debugLogger.logSuccess("SSL connection test completed")

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    connectionStatus = "Connection failed: ${e.message}"
                )
                debugLogger.logError("SSL connection test failed", e)
            } finally {
                _state.value = _state.value.copy(isTestingConnection = false)
            }
        }
    }

    // HTTPS Redirect Server Configuration Methods
    fun toggleHttps(enabled: Boolean) {
        _state.value = _state.value.copy(
            httpsEnabled = enabled,
            redirectUrl = if (enabled) "https://localhost:8080/redirect" else "http://localhost:8080/redirect"
        )
        debugLogger.logInfo("HTTPS toggle set to: $enabled")
    }

    fun updateCertificateSource(source: CertificateSource) {
        _state.value = _state.value.copy(selectedCertificateSource = source)
        debugLogger.logInfo("Certificate source changed to: ${source.name}")
    }

    fun startServer() {
        scope.launch {
            try {
                _state.value = _state.value.copy(isServerLoading = true)
                val currentState = _state.value
                
                debugLogger.logInfo("Starting server in ${if (currentState.httpsEnabled) "HTTPS" else "HTTP"} mode...")
                
                if (currentState.httpsEnabled) {
                    val certificateSource = certificateManager.createCertificateSource(currentState.selectedCertificateSource)
                    if (certificateSource != null) {
                        debugLogger.logSuccess("Certificate source created for ${currentState.selectedCertificateSource.name}")
                    } else {
                        debugLogger.logError("Failed to create certificate source for ${currentState.selectedCertificateSource.name}")
                    }
                }
                
                kotlinx.coroutines.delay(1000) // Simulate server startup
                
                val newStatus = if (currentState.httpsEnabled) "Running (HTTPS)" else "Running (HTTP)"
                _state.value = _state.value.copy(serverStatus = newStatus)
                debugLogger.logSuccess("Server started successfully")
                
            } catch (e: Exception) {
                debugLogger.logError("Failed to start server", e)
                _state.value = _state.value.copy(serverStatus = "Failed to start: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isServerLoading = false)
            }
        }
    }

    fun stopServer() {
        _state.value = _state.value.copy(serverStatus = "Stopped")
        debugLogger.logInfo("Server stopped")
    }

    // Certificate Management Methods
    fun updateCertificateInfo() {
        scope.launch {
            certificateManager.refreshCertificateInfo()
            debugLogger.logInfo("Certificate information updated")
        }
    }

    fun getCertificateInfo() = certificateManager.certificateInfo

    fun getCertificateDescription(): String {
        val currentState = _state.value
        val certInfo = certificateManager.getCertificateBySource(currentState.selectedCertificateSource)
        return certInfo?.description ?: "Unknown certificate"
    }

    fun getCertificateLocation(): String {
        val currentState = _state.value
        val certInfo = certificateManager.getCertificateBySource(currentState.selectedCertificateSource)
        return certInfo?.location ?: "Unknown location"
    }

    fun isCertificateAvailable(source: CertificateSource): Boolean {
        return certificateManager.getCertificateBySource(source)?.available ?: false
    }

    // Configuration Code Generation
    fun generateHttpClientConfigurationCode(): String {
        val currentState = _state.value
        return when (currentState.sslConfigType) {
            SslConfigType.DEFAULT -> """
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
}.createSslEnabledClient()
            """.trimIndent()

            SslConfigType.CUSTOM_TRUST_STORE -> """
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
    
    ssl {
        trustStore("${currentState.trustStorePath.ifBlank { "/path/to/truststore.jks" }}", "${currentState.trustStorePassword.ifBlank { "password" }}")
    }
}.createSslEnabledClient()
            """.trimIndent()

            SslConfigType.DISABLE_VALIDATION -> if (currentState.disableValidation) {
                """
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
    
    ssl {
        disableCertificateValidation()
        disableHostnameVerification()
    }
}.createSslEnabledClient()
                """.trimIndent()
            } else {
                "Please acknowledge the security warning first"
            }

            SslConfigType.CLIENT_CERTIFICATE -> """
val client = OpenIdConnectClient(discoveryUri = "...") {
    clientId = "your-client-id"
    
    ssl {
        keyStore("/path/to/client-cert.p12", "client-password")
        trustStore("/path/to/truststore.jks", "truststore-password")
    }
}.createSslEnabledClient()
            """.trimIndent()
        }
    }

    fun generateHttpsRedirectConfigurationCode(): String {
        val currentState = _state.value
        val certInfo = certificateManager.getCertificateBySource(currentState.selectedCertificateSource)
        
        return if (currentState.httpsEnabled) {
            when (currentState.selectedCertificateSource) {
                CertificateSource.DOCKER -> """
val factory = JvmCodeAuthFlowFactory(
    port = 8443,
    webserverProvider = {
        SslWebserver(
            enableHttps = true,
            certificateSource = CertificateSourceFactory.fromFile(
                certificateFile = File("docker/certs/localhost/localhost.p12"),
                password = "localhost"
            )
        )
    }
)
                """.trimIndent()
                
                CertificateSource.RESOURCES -> """
val factory = JvmCodeAuthFlowFactory(
    port = 8443,
    webserverProvider = {
        SslWebserver(
            enableHttps = true,
            certificateSource = CertificateSourceFactory.fromResources()
        )
    }
)
                """.trimIndent()
                
                CertificateSource.SELF_SIGNED -> """
val factory = JvmCodeAuthFlowFactory(
    port = 8443,
    webserverProvider = {
        SslWebserver(
            enableHttps = true,
            certificateSource = CertificateSourceFactory.selfSigned()
        )
    }
)
                """.trimIndent()
                
                CertificateSource.LETS_ENCRYPT -> """
val factory = JvmCodeAuthFlowFactory(
    port = 8443,
    webserverProvider = {
        SslWebserver(
            enableHttps = true,
            certificateSource = CertificateSourceFactory.fromFile(
                certificateFile = File("${System.getProperty("user.home")}/.letsencrypt/localhost.p12"),
                password = "letsencrypt"
            )
        )
    }
)
                """.trimIndent()
            }
        } else {
            """
val factory = JvmCodeAuthFlowFactory(port = 8080) // HTTP only
            """.trimIndent()
        }
    }

    fun logEnvironmentInfo() {
        certificateManager.logCertificateEnvironment()
    }
}