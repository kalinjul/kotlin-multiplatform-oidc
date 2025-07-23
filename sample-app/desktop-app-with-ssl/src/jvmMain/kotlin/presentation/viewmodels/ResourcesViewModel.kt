package presentation.viewmodels

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.ssl.CertificateSourceFactory
import org.publicvalue.multiplatform.oidc.appsupport.ssl.ResourcesCertificateSource
import presentation.state.DebugLogger
import java.util.Date

data class ResourcesState(
    val selectedCertificate: String = "localhost.p12",
    val certificatePassword: String = "localhost",
    val resourcesStatus: String = "Checking resources...",
    val certificateDetails: String = "",
    val availableResources: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val testResult: String = ""
)

@OptIn(ExperimentalOpenIdConnect::class)
class ResourcesViewModel {
    private val _state = mutableStateOf(ResourcesState())
    val state: State<ResourcesState> = _state
    
    val debugLogger = DebugLogger()
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    init {
        checkAvailableResources()
    }
    
    fun updateSelectedCertificate(certificate: String) {
        _state.value = _state.value.copy(selectedCertificate = certificate)
    }
    
    fun updateCertificatePassword(password: String) {
        _state.value = _state.value.copy(certificatePassword = password)
    }
    
    fun testCertificate() {
        scope.launch {
            try {
                _state.value = _state.value.copy(isLoading = true)
                debugLogger.clear()
                
                val currentState = _state.value
                debugLogger.logInfo("Testing resource certificate loading")
                debugLogger.logInfo("Certificate: ${currentState.selectedCertificate}")
                debugLogger.logInfo("Password: ${currentState.certificatePassword}")
                
                _state.value = currentState.copy(testResult = "Loading resource certificate...")
                
                val resourcePath = if (currentState.selectedCertificate.startsWith("/")) {
                    currentState.selectedCertificate
                } else {
                    "/ssl/certificates/${currentState.selectedCertificate}"
                }
                
                debugLogger.logInfo("Full resource path: $resourcePath")
                
                val certificateSource = CertificateSourceFactory.fromResources(
                    resourcePath = resourcePath,
                    password = currentState.certificatePassword
                )
                
                debugLogger.logInfo("Certificate source created")
                
                val certInfo = certificateSource.getCertificate("localhost")
                
                debugLogger.logSuccess("Certificate loaded successfully!")
                _state.value = _state.value.copy(testResult = "Certificate loaded successfully!")
                
                val cert = certInfo.certificate
                val certificateDetails = buildString {
                    appendLine("Subject: ${cert.subjectX500Principal.name}")
                    appendLine("Issuer: ${cert.issuerX500Principal.name}")
                    appendLine("Valid From: ${cert.notBefore}")
                    appendLine("Valid Until: ${cert.notAfter}")
                    appendLine("Serial Number: ${cert.serialNumber}")
                    appendLine("Signature Algorithm: ${cert.sigAlgName}")
                    
                    val now = Date()
                    val isValid = now.after(cert.notBefore) && now.before(cert.notAfter)
                    appendLine("Currently Valid: $isValid")
                }
                
                _state.value = _state.value.copy(certificateDetails = certificateDetails)
                debugLogger.logInfo("Certificate details populated")
                
            } catch (e: Exception) {
                debugLogger.logError("Certificate loading failed: ${e.message}", e)
                _state.value = _state.value.copy(
                    testResult = "Failed: ${e.message}",
                    certificateDetails = ""
                )
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun reset() {
        _state.value = _state.value.copy(
            certificateDetails = "",
            testResult = ""
        )
        debugLogger.clear()
    }
    
    private fun checkAvailableResources() {
        scope.launch {
            try {
                debugLogger.logInfo("Checking available resource certificates...")
                val resourcesSource = CertificateSourceFactory.fromResources()
                if (resourcesSource is ResourcesCertificateSource) {
                    val available = resourcesSource.listAvailableResources()
                    
                    _state.value = _state.value.copy(
                        availableResources = available,
                        resourcesStatus = if (available.isNotEmpty()) {
                            "Found ${available.size} resource certificate(s)"
                        } else {
                            "No resource certificates found"
                        }
                    )
                    
                    if (available.isNotEmpty()) {
                        debugLogger.logInfo("Found resource certificates: ${available.joinToString(", ")}")
                    } else {
                        debugLogger.logInfo("No resource certificates found in classpath")
                    }
                    
                    val hasDefaultCert = resourcesSource.hasResourceCertificate("localhost")
                    debugLogger.logInfo("Default localhost certificate available: $hasDefaultCert")
                } else {
                    _state.value = _state.value.copy(
                        resourcesStatus = "Error: Could not create resources certificate source"
                    )
                    debugLogger.logError("Failed to create ResourcesCertificateSource")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    resourcesStatus = "Error checking resources: ${e.message}"
                )
                debugLogger.logError("Exception checking resources: ${e.message}", e)
            }
        }
    }
}