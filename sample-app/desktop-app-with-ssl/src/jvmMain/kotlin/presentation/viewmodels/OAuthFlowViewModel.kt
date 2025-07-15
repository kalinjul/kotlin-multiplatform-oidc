package presentation.viewmodels

import androidx.compose.runtime.*
import kotlinx.coroutines.*
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.ssl.*
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SslWebserver
import org.publicvalue.multiplatform.oidc.ssl.createSslEnabledOpenIdConnectClient
import presentation.CertificateManager
import presentation.CertificateSource
import presentation.state.DebugLogger
import java.io.File
import java.security.MessageDigest
import java.security.cert.X509Certificate

data class OAuthFlowState(
    val discoveryUrl: String = "http://localhost:7080/realms/playground/.well-known/openid-configuration",
    val clientId: String = "basic-client",
    val clientSecret: String = "basic-client-secret",
    val oauthScopes: String = "openid profile email",
    val useHttps: Boolean = false,
    val useHttpsKeycloak: Boolean = false,
    val flowStatus: String = "Ready",
    val accessToken: String = "",
    val serverDetails: String = "",
    val isExecuting: Boolean = false
)

@OptIn(ExperimentalOpenIdConnect::class)
class OAuthFlowViewModel {
    private val _state = mutableStateOf(OAuthFlowState())
    val state: State<OAuthFlowState> = _state
    
    val debugLogger = DebugLogger()
    private val certificateManager = CertificateManager(debugLogger)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    fun updateDiscoveryUrl(url: String) {
        _state.value = _state.value.copy(discoveryUrl = url)
    }
    
    fun updateClientId(id: String) {
        _state.value = _state.value.copy(clientId = id)
    }
    
    fun updateClientSecret(secret: String) {
        _state.value = _state.value.copy(clientSecret = secret)
    }
    
    fun updateOAuthScopes(scopes: String) {
        _state.value = _state.value.copy(oauthScopes = scopes)
    }
    
    fun toggleHttps(enabled: Boolean) {
        _state.value = _state.value.copy(useHttps = enabled)
        debugLogger.logInfo("HTTPS toggle set to: $enabled")
    }
    
    fun toggleHttpsKeycloak(enabled: Boolean) {
        val newDiscoveryUrl = if (enabled) {
            "https://localhost:7001/realms/playground/.well-known/openid-configuration"
        } else {
            "http://localhost:7080/realms/playground/.well-known/openid-configuration"
        }
        _state.value = _state.value.copy(
            useHttpsKeycloak = enabled,
            discoveryUrl = newDiscoveryUrl
        )
        debugLogger.logInfo("HTTPS Keycloak toggle set to: $enabled")
        debugLogger.logInfo("Discovery URL updated to: $newDiscoveryUrl")
    }
    
    fun startOAuthFlow() {
        scope.launch {
            try {
                _state.value = _state.value.copy(isExecuting = true)
                debugLogger.clear()
                
                val currentState = _state.value
                debugLogger.logInfo("Starting OAuth flow with ${if (currentState.useHttps) "HTTPS" else "HTTP"}")
                debugLogger.logInfo("Using application-level coroutine scope to prevent UI cancellation")
                
                _state.value = currentState.copy(flowStatus = "Starting OAuth flow...")
                
                withTimeout(300_000) {
                    debugLogger.logInfo("Creating OIDC client with redirect URI: ${getRedirectUri()}")
                    
                    val config = org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig(currentState.discoveryUrl).apply {
                        this.clientId = currentState.clientId
                        this.clientSecret = currentState.clientSecret
                        redirectUri = getRedirectUri()
                        this.scope = currentState.oauthScopes
                        
                        if (currentState.useHttpsKeycloak) {
                            debugLogger.logInfo("Configuring SSL trust for HTTPS Keycloak")
                            configureSslForKeycloak(this)
                        }
                    }
                    
                    val client = createSslEnabledOpenIdConnectClient(config)
                    debugLogger.logInfo("OIDC client created successfully")
                    
                    debugLogger.logInfo("Creating auth flow factory...")
                    val factory = if (currentState.useHttps) {
                        createHttpsFactory()
                    } else {
                        debugLogger.logInfo("Creating HTTP factory with port 8080")
                        JvmCodeAuthFlowFactory(port = 8080).also {
                            _state.value = _state.value.copy(serverDetails = "Using HTTP on port 8080 (no SSL)")
                        }
                    }
                    
                    debugLogger.logInfo("Discovering OIDC configuration...")
                    _state.value = _state.value.copy(flowStatus = "Discovering OIDC configuration...")
                    
                    client.discover()
                    debugLogger.logInfo("✅ OIDC discovery completed successfully")
                    
                    debugLogger.logInfo("Creating auth flow...")
                    _state.value = _state.value.copy(flowStatus = "Creating auth flow...")
                    val authFlow = factory.createAuthFlow(client)
                    debugLogger.logInfo("Auth flow created successfully")
                    
                    if (currentState.useHttps) {
                        debugLogger.logInfo("Starting HTTPS redirect server on port 8080...")
                        debugLogger.logInfo("Browser will show security warning for self-signed certificate")
                        debugLogger.logInfo("Click 'Advanced' -> 'Proceed to localhost' to continue")
                    }
                    
                    debugLogger.logInfo("Starting authorization flow - opening browser...")
                    _state.value = _state.value.copy(flowStatus = "Waiting for user authorization...")
                    val tokens = authFlow.getAccessToken()
                    
                    debugLogger.logSuccess("Authorization completed successfully!")
                    debugLogger.logInfo("Access token received: ${tokens.access_token.take(50)}...")
                    
                    _state.value = _state.value.copy(
                        accessToken = tokens.access_token,
                        flowStatus = "OAuth flow completed successfully!"
                    )
                }
                
            } catch (e: CancellationException) {
                debugLogger.logInfo("OAuth flow was cancelled - this is expected behavior")
                _state.value = _state.value.copy(flowStatus = "OAuth flow cancelled")
            } catch (e: TimeoutCancellationException) {
                debugLogger.logError("OAuth flow timed out after 5 minutes")
                _state.value = _state.value.copy(flowStatus = "OAuth flow timed out - please try again")
            } catch (e: Exception) {
                debugLogger.logError("OAuth flow failed: ${e.javaClass.simpleName}: ${e.message}", e)
                _state.value = _state.value.copy(flowStatus = "Error: ${e.message}")
            } finally {
                _state.value = _state.value.copy(isExecuting = false)
            }
        }
    }
    
    fun resetFlow() {
        _state.value = _state.value.copy(
            flowStatus = "Ready",
            accessToken = "",
            serverDetails = ""
        )
        debugLogger.clear()
    }
    
    private fun getRedirectUri(): String {
        return if (_state.value.useHttps) "https://localhost:8443/redirect" else "http://localhost:8080/redirect"
    }
    
    private fun getKeycloakDisplayUrl(): String {
        return if (_state.value.useHttpsKeycloak) "https://localhost:7001" else "http://localhost:7080"
    }
    
    private fun configureSslForKeycloak(config: org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig) {
        debugLogger.logInfo("=== Configuring SSL for HTTPS Keycloak ===")
        
        try {
            // Try multiple approaches for SSL configuration
            val caCertFile = File("docker/certs/ca/ca.crt")
            debugLogger.logInfo("Checking CA certificate at: ${caCertFile.absolutePath}")
            debugLogger.logInfo("CA certificate exists: ${caCertFile.exists()}")
            debugLogger.logInfo("CA certificate readable: ${caCertFile.canRead()}")
            debugLogger.logInfo("CA certificate size: ${if (caCertFile.exists()) caCertFile.length() else "N/A"} bytes")
            
            if (caCertFile.exists() && caCertFile.canRead()) {
                debugLogger.logInfo("✅ Loading demo CA certificate...")
                
                // Load the CA certificate and add it to trusted certificates
                val certificateFactory = java.security.cert.CertificateFactory.getInstance("X.509")
                val caCert = caCertFile.inputStream().use { inputStream ->
                    certificateFactory.generateCertificate(inputStream) as java.security.cert.X509Certificate
                }
                
                debugLogger.logInfo("CA Certificate Details:")
                debugLogger.logInfo("  Subject: ${caCert.subjectX500Principal}")
                debugLogger.logInfo("  Issuer: ${caCert.issuerX500Principal}")
                debugLogger.logInfo("  Valid from: ${caCert.notBefore}")
                debugLogger.logInfo("  Valid until: ${caCert.notAfter}")
                debugLogger.logInfo("  Serial Number: ${caCert.serialNumber}")
                debugLogger.logInfo("  SHA-256 Fingerprint: ${certificateManager.getCertificateFingerprint(caCert)}")
                
                // Apply SSL configuration with certificate trust and fallback options
                config.ssl {
                    addTrustedCertificate(caCert)
                    debugLogger.logInfo("✅ Added demo CA certificate to trusted certificates")
                    
                    // Also disable hostname verification for demo localhost certificates
                    debugLogger.logInfo("⚠️ Disabling hostname verification for demo purposes")
                    disableHostnameVerification()
                }
                
                debugLogger.logInfo("✅ SSL configuration completed with certificate trust")
                
            } else {
                debugLogger.logWarning("❌ Demo CA certificate not accessible")
                debugLogger.logWarning("Falling back to unsafe SSL for demo purposes")
                applyUnsafeSslForDemo(config)
            }
        } catch (e: Exception) {
            debugLogger.logError("❌ SSL configuration failed: ${e.javaClass.simpleName}: ${e.message}")
            debugLogger.logError("Stack trace: ${e.stackTrace.take(3).joinToString { it.toString() }}")
            debugLogger.logWarning("⚠️ Falling back to unsafe SSL for demo purposes")
            applyUnsafeSslForDemo(config)
        }
    }
    
    private fun applyUnsafeSslForDemo(config: org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig) {
        try {
            debugLogger.logInfo("🔓 Applying unsafe SSL configuration for demo...")
            config.ssl {
                disableCertificateValidation()
                disableHostnameVerification()
            }
            debugLogger.logWarning("⚠️ UNSAFE SSL configured - accepts all certificates!")
            debugLogger.logWarning("⚠️ This is for demo purposes only - never use in production!")
        } catch (e: Exception) {
            debugLogger.logError("❌ Even unsafe SSL configuration failed: ${e.message}")
            debugLogger.logError("This suggests a fundamental SSL configuration issue")
        }
    }
    
    
    private suspend fun createHttpsFactory(): JvmCodeAuthFlowFactory {
        certificateManager.logCertificateEnvironment()
        
        // Try certificate sources in order of preference
        val certificateSources = listOf(
            CertificateSource.LETS_ENCRYPT,
            CertificateSource.DOCKER,
            CertificateSource.RESOURCES,
            CertificateSource.SELF_SIGNED
        )
        
        for (source in certificateSources) {
            debugLogger.logInfo("=== Attempting ${source.name} Certificate ===")
            
            val certificateInfo = certificateManager.getCertificateBySource(source)
            if (certificateInfo?.available == true) {
                val certificateSource = certificateManager.createCertificateSource(source)
                if (certificateSource != null) {
                    try {
                        _state.value = _state.value.copy(
                            flowStatus = "Using ${source.name.lowercase().replace("_", " ")} certificate..."
                        )
                        
                        val httpsFactory = JvmCodeAuthFlowFactory(
                            port = 8443,
                            webserverProvider = {
                                SslWebserver(
                                    enableHttps = true,
                                    certificateSource = certificateSource
                                )
                            }
                        )
                        
                        debugLogger.logSuccess("HTTPS factory created with ${source.name} certificate")
                        _state.value = _state.value.copy(
                            serverDetails = "Using ${certificateInfo.description}: ${certificateInfo.location}"
                        )
                        
                        return httpsFactory
                        
                    } catch (e: Exception) {
                        debugLogger.logError("${source.name} certificate failed: ${e.message}")
                    }
                }
            } else {
                debugLogger.logInfo("${source.name} certificate not available")
            }
        }
        
        throw RuntimeException("HTTPS setup failed - no valid certificate sources available")
    }
    
    
}