package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import presentation.CertificateSource
import presentation.viewmodels.SslConfigurationViewModel
import presentation.viewmodels.SslConfigType
import presentation.viewmodels.SslConfigurationState
import ui.components.DebugLogsSection
import ui.components.InfoCard
import ui.components.StatusHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SslConfigurationScreen() {
    val viewModel = remember { SslConfigurationViewModel() }
    val state by viewModel.state
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HTTP Client SSL Configuration Section
        HttpClientSslSection(
            state = state,
            onSslConfigTypeChange = viewModel::updateSslConfigType,
            onTrustStorePathChange = viewModel::updateTrustStorePath,
            onTrustStorePasswordChange = viewModel::updateTrustStorePassword,
            onDisableValidationChange = viewModel::updateDisableValidation,
            onTestConnection = viewModel::testConnection,
            onGenerateCode = viewModel::generateHttpClientConfigurationCode
        )
        
        HorizontalDivider()
        
        // HTTPS Redirect Server Configuration Section
        HttpsRedirectSection(
            state = state,
            onHttpsToggle = viewModel::toggleHttps,
            onCertificateSourceChange = viewModel::updateCertificateSource,
            onStartServer = viewModel::startServer,
            onStopServer = viewModel::stopServer,
            onGenerateCode = viewModel::generateHttpsRedirectConfigurationCode,
            getCertificateInfo = viewModel::getCertificateInfo,
            isCertificateAvailable = viewModel::isCertificateAvailable
        )
        
        HorizontalDivider()
        
        // Debug Logs Section
        DebugLogsSection(logs = viewModel.debugLogger.logs)
    }
}

@Composable
private fun HttpClientSslSection(
    state: SslConfigurationState,
    onSslConfigTypeChange: (SslConfigType) -> Unit,
    onTrustStorePathChange: (String) -> Unit,
    onTrustStorePasswordChange: (String) -> Unit,
    onDisableValidationChange: (Boolean) -> Unit,
    onTestConnection: () -> Unit,
    onGenerateCode: () -> String
) {
    InfoCard(
        title = "HTTP Client SSL Configuration"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Configure SSL/TLS settings for HTTP client connections to OIDC providers",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // SSL Configuration Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "SSL Configuration Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    SslConfigType.values().forEach { type ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = state.sslConfigType == type,
                                onClick = { onSslConfigTypeChange(type) }
                            )
                            Text(
                                text = type.displayName,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
            
            // Configuration-specific fields
            when (state.sslConfigType) {
                SslConfigType.CUSTOM_TRUST_STORE -> {
                    CustomTrustStoreFields(
                        trustStorePath = state.trustStorePath,
                        trustStorePassword = state.trustStorePassword,
                        onTrustStorePathChange = onTrustStorePathChange,
                        onTrustStorePasswordChange = onTrustStorePasswordChange
                    )
                }
                SslConfigType.DISABLE_VALIDATION -> {
                    DisableValidationWarning(
                        disableValidation = state.disableValidation,
                        onDisableValidationChange = onDisableValidationChange
                    )
                }
                else -> {}
            }
            
            // Connection Test
            ConnectionTestSection(
                connectionStatus = state.connectionStatus,
                isTestingConnection = state.isTestingConnection,
                onTestConnection = onTestConnection
            )
            
            // Generated Code
            GeneratedCodeSection(
                title = "Generated HTTP Client Code",
                code = onGenerateCode()
            )
        }
    }
}

@Composable
private fun HttpsRedirectSection(
    state: SslConfigurationState,
    onHttpsToggle: (Boolean) -> Unit,
    onCertificateSourceChange: (CertificateSource) -> Unit,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit,
    onGenerateCode: () -> String,
    getCertificateInfo: () -> List<presentation.CertificateInfo>,
    isCertificateAvailable: (CertificateSource) -> Boolean
) {
    InfoCard(
        title = "HTTPS Redirect Server Configuration"
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Configure HTTPS settings for the OAuth redirect server",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // HTTPS Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = state.httpsEnabled,
                    onCheckedChange = onHttpsToggle
                )
                Text(
                    text = "Enable HTTPS",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            // Certificate Source Selection (only when HTTPS is enabled)
            if (state.httpsEnabled) {
                CertificateSourceSelection(
                    selectedSource = state.selectedCertificateSource,
                    onSourceChange = onCertificateSourceChange,
                    getCertificateInfo = getCertificateInfo,
                    isCertificateAvailable = isCertificateAvailable
                )
            }
            
            // Server Controls
            ServerControlsSection(
                serverStatus = state.serverStatus,
                isServerLoading = state.isServerLoading,
                onStartServer = onStartServer,
                onStopServer = onStopServer
            )
            
            // Redirect URL Display
            RedirectUrlDisplay(redirectUrl = state.redirectUrl)
            
            // Generated Code
            GeneratedCodeSection(
                title = "Generated HTTPS Redirect Code",
                code = onGenerateCode()
            )
        }
    }
}

@Composable
private fun CustomTrustStoreFields(
    trustStorePath: String,
    trustStorePassword: String,
    onTrustStorePathChange: (String) -> Unit,
    onTrustStorePasswordChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = trustStorePath,
            onValueChange = onTrustStorePathChange,
            label = { Text("Trust Store Path") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Folder, contentDescription = null) }
        )
        
        OutlinedTextField(
            value = trustStorePassword,
            onValueChange = onTrustStorePasswordChange,
            label = { Text("Trust Store Password") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
        )
    }
}

@Composable
private fun DisableValidationWarning(
    disableValidation: Boolean,
    onDisableValidationChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    "Security Warning",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            
            Text(
                "Disabling SSL validation is extremely dangerous and should only be used in development environments.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = disableValidation,
                    onCheckedChange = onDisableValidationChange
                )
                Text(
                    text = "I understand the security risks",
                    modifier = Modifier.padding(start = 8.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun ConnectionTestSection(
    connectionStatus: String,
    isTestingConnection: Boolean,
    onTestConnection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Connection Test",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                connectionStatus,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Button(
                onClick = onTestConnection,
                enabled = !isTestingConnection,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTestingConnection) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.NetworkCheck, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Connection")
            }
        }
    }
}

@Composable
private fun CertificateSourceSelection(
    selectedSource: CertificateSource,
    onSourceChange: (CertificateSource) -> Unit,
    getCertificateInfo: () -> List<presentation.CertificateInfo>,
    isCertificateAvailable: (CertificateSource) -> Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Certificate Source",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            CertificateSource.values().forEach { source ->
                val isAvailable = isCertificateAvailable(source)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSource == source,
                        onClick = { onSourceChange(source) },
                        enabled = isAvailable
                    )
                    
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = source.name.replace("_", " ").lowercase()
                                .replaceFirstChar { it.uppercase() },
                            color = if (isAvailable) MaterialTheme.colorScheme.onSurface 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        
                        if (!isAvailable) {
                            Text(
                                text = "Not available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerControlsSection(
    serverStatus: String,
    isServerLoading: Boolean,
    onStartServer: () -> Unit,
    onStopServer: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Server Controls",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "Status: $serverStatus",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onStartServer,
                    enabled = !isServerLoading && serverStatus == "Stopped",
                    modifier = Modifier.weight(1f)
                ) {
                    if (isServerLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Server")
                }
                
                Button(
                    onClick = onStopServer,
                    enabled = !isServerLoading && serverStatus.startsWith("Running"),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stop Server")
                }
            }
        }
    }
}

@Composable
private fun RedirectUrlDisplay(redirectUrl: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Redirect URL",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            SelectionContainer {
                Text(
                    text = redirectUrl,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun GeneratedCodeSection(
    title: String,
    code: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            SelectionContainer {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}