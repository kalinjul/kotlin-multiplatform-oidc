package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import presentation.viewmodels.ResourcesViewModel
import presentation.viewmodels.ResourcesState
import ui.components.ConfigurationCard
import ui.components.DebugLogsSection
import ui.components.InfoCard
import ui.components.StatusInfoCard
import ui.theme.SectionTitle

@OptIn(ExperimentalOpenIdConnect::class)
@Composable
fun ReferenceScreen() {
    val viewModel = remember { ResourcesViewModel() }
    val state by viewModel.state
    val debugLogs by remember { derivedStateOf { viewModel.debugLogger.logs } }
    
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "SSL Configuration Reference",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "SSL configuration examples, certificate testing, and best practices.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            // Certificate Testing Section
            CertificateTestingSection(
                state = state,
                onSelectedCertificateChange = viewModel::updateSelectedCertificate,
                onCertificatePasswordChange = viewModel::updateCertificatePassword,
                onTestCertificate = viewModel::testCertificate,
                onReset = viewModel::reset
            )

            HorizontalDivider()

            // Configuration Examples Section
            ConfigurationExamplesSection()

            HorizontalDivider()

            // Debug Logs
            DebugLogsSection(
                logs = debugLogs,
                maxHeight = 200
            )

            HorizontalDivider()

            // Best Practices Section
            BestPracticesSection()
        }
    }
}

@Composable
private fun CertificateTestingSection(
    state: ResourcesState,
    onSelectedCertificateChange: (String) -> Unit,
    onCertificatePasswordChange: (String) -> Unit,
    onTestCertificate: () -> Unit,
    onReset: () -> Unit
) {
    SectionTitle("Certificate Testing")

    StatusInfoCard(
        title = "Resource Certificate Status",
        statusItems = buildList {
            add("Status" to state.resourcesStatus)
            add("Available Certificates" to state.availableResources.size.toString())
            
            if (state.availableResources.isNotEmpty()) {
                state.availableResources.forEach { resource ->
                    add("Resource" to resource)
                }
            }
        }
    )

    OutlinedTextField(
        value = state.selectedCertificate,
        onValueChange = onSelectedCertificateChange,
        label = { Text("Certificate Resource") },
        placeholder = { Text("localhost.p12") },
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            Text("Resource path relative to /ssl/certificates/")
        }
    )
    
    OutlinedTextField(
        value = state.certificatePassword,
        onValueChange = onCertificatePasswordChange,
        label = { Text("Certificate Password") },
        placeholder = { Text("localhost") },
        modifier = Modifier.fillMaxWidth()
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onTestCertificate,
            modifier = Modifier.weight(1f),
            enabled = !state.isLoading && state.selectedCertificate.isNotBlank()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Folder, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.isLoading) "Loading..." else "Test Certificate")
        }
        
        OutlinedButton(
            onClick = onReset,
            modifier = Modifier.weight(1f),
            enabled = !state.isLoading
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset")
        }
    }
    
    if (state.testResult.isNotBlank()) {
        StatusInfoCard(
            title = "Test Result",
            statusItems = listOf("Result" to state.testResult)
        )
    }

    if (state.certificateDetails.isNotBlank()) {
        InfoCard(
            title = "Certificate Details"
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = state.certificateDetails,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ConfigurationExamplesSection() {
    SectionTitle("Configuration Examples")

    ConfigurationCard(
        title = "Auto-Detection (Recommended)",
        configCode = """
// Automatic certificate source detection
val certificateSource = CertificateSourceFactory.autoDetect()
val factory = JvmCodeAuthFlowFactory {
    SslWebserver(enableHttps = true, certificateSource = certificateSource)
}

// Priority order:
// 1. Let's Encrypt (if configured)
// 2. User-provided files (keystore.jks, keystore.p12)
// 3. Docker volume certificates
// 4. Resource certificates (bundled with app)
// 5. Auto-generated self-signed certificates
        """.trimIndent()
    )

    ConfigurationCard(
        title = "File-based Certificates",
        configCode = """
// Load certificate from file system
val certificateSource = CertificateSourceFactory.fromFile(
    keystorePath = "/path/to/keystore.p12",
    password = "keystorePassword"
)

val factory = JvmCodeAuthFlowFactory {
    SslWebserver(enableHttps = true, certificateSource = certificateSource)
}

// Alternative: JKS keystore
val jksSource = CertificateSourceFactory.fromFile(
    keystorePath = "/path/to/keystore.jks",
    password = "keystorePassword"
)
        """.trimIndent()
    )

    ConfigurationCard(
        title = "Resource Certificates",
        configCode = """
// Auto-detect resource certificate
val certificateSource = CertificateSourceFactory.fromResources()
val factory = JvmCodeAuthFlowFactory {
    SslWebserver(enableHttps = true, certificateSource = certificateSource)
}

// Specific resource certificate
val specificSource = CertificateSourceFactory.fromResources(
    resourcePath = "/ssl/certificates/localhost.p12",
    password = "localhost"
)
        """.trimIndent()
    )

    ConfigurationCard(
        title = "Self-signed Certificates",
        configCode = """
// Generate self-signed certificate automatically
val certificateSource = CertificateSourceFactory.selfSigned()

val factory = JvmCodeAuthFlowFactory {
    SslWebserver(enableHttps = true, certificateSource = certificateSource)
}

// Note: Self-signed certificates will show security warnings
// Use only for development/testing
        """.trimIndent()
    )

    ConfigurationCard(
        title = "Complete OAuth Flow with SSL",
        configCode = """
// Complete OAuth flow with SSL webserver
val certificateSource = CertificateSourceFactory.autoDetect()
val factory = JvmCodeAuthFlowFactory {
    SslWebserver(enableHttps = true, certificateSource = certificateSource)
}

val client = OpenIdConnectClient(config) {
    codeAuthFlowFactory = factory
}

// Start OAuth flow
val result = client.authCodeFlow()
    .getAuthorizationUrl()
    .also { url -> 
        // Open browser or handle URL
        Desktop.getDesktop().browse(URI(url))
    }
    .getAccessToken()

val accessToken = result.access_token
        """.trimIndent()
    )

    ConfigurationCard(
        title = "HTTP Client SSL Configuration",
        configCode = """
// Basic SSL configuration
val client = OpenIdConnectClient(discoveryUri = "https://...") {
    clientId = "your-client-id"
}.createSslEnabledClient()

// Custom trust store
val client = OpenIdConnectClient(discoveryUri = "https://...") {
    clientId = "your-client-id"
    ssl {
        trustStore("/path/to/truststore.jks", "password")
    }
}.createSslEnabledClient()

// Development mode (unsafe)
val client = OpenIdConnectClient(discoveryUri = "https://...") {
    clientId = "your-client-id"
    ssl {
        disableCertificateValidation()
        disableHostnameVerification()
    }
}.createSslEnabledClient()
        """.trimIndent()
    )
}

@Composable
private fun BestPracticesSection() {
    SectionTitle("Best Practices & Information")

    InfoCard(
        title = "Certificate Source Priority"
    ) {
        val priorities = listOf(
            "1. Let's Encrypt" to "Highest priority - production-ready trusted certificates",
            "2. User Files" to "User-provided keystore files (JKS, PKCS12)",
            "3. Docker Volume" to "Certificates mounted from Docker volumes",
            "4. Resources" to "Certificates bundled with the application",
            "5. Self-signed" to "Auto-generated certificates (development only)"
        )
        
        priorities.forEach { (priority, description) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    priority,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.width(120.dp)
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    InfoCard(
        title = "SSL Configuration Best Practices"
    ) {
        val practices = listOf(
            "Use Let's Encrypt for production deployments",
            "Always use staging environment for testing Let's Encrypt",
            "Store certificates securely with proper file permissions",
            "Use strong passwords for keystore files",
            "Regularly rotate certificates before expiration",
            "Monitor certificate expiration dates",
            "Use TLS 1.3 when possible for better security",
            "Implement proper certificate validation in clients",
            "Keep private keys secure and never commit them to version control",
            "Test certificate configurations in development environments"
        )
        
        practices.forEach { practice ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "• ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    practice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    InfoCard(
        title = "Resources Certificate Information"
    ) {
        val info = listOf(
            "Certificates are loaded from the application's resources folder",
            "Default path: /ssl/certificates/",
            "Supported formats: PKCS12 (.p12, .pfx), JKS (.jks)",
            "Certificates are bundled with the application JAR",
            "Perfect for distributing pre-configured certificates",
            "Resources are checked in certificate auto-detection",
            "Priority: after file certificates, before self-signed"
        )
        
        info.forEach { infoItem ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "• ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    infoItem,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}