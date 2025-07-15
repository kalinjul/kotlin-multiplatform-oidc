package ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import presentation.viewmodels.OAuthFlowViewModel
import ui.components.DebugLogsSection
import ui.components.StatusInfoCard
import ui.theme.SectionTitle

@OptIn(ExperimentalOpenIdConnect::class)
@Composable
fun OAuthFlowScreen() {
    val viewModel = remember { OAuthFlowViewModel() }
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
                text = "OAuth Flow with SSL Demo",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Perform a complete OAuth authorization code flow using SSL configuration.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            SectionTitle("OIDC Provider Configuration:")
            
            OutlinedTextField(
                value = state.discoveryUrl,
                onValueChange = { viewModel.updateDiscoveryUrl(it) },
                label = { Text("Discovery URL") },
                placeholder = { Text("http://localhost:7080/realms/master/.well-known/openid-configuration") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = state.clientId,
                onValueChange = { viewModel.updateClientId(it) },
                label = { Text("Client ID") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = state.clientSecret,
                onValueChange = { viewModel.updateClientSecret(it) },
                label = { Text("Client Secret") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = state.oauthScopes,
                onValueChange = { viewModel.updateOAuthScopes(it) },
                label = { Text("OAuth Scopes") },
                placeholder = { Text("openid profile email") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Use HTTPS for redirect server:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.useHttps,
                    onCheckedChange = { viewModel.toggleHttps(it) }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Use HTTPS for Keycloak:",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = state.useHttpsKeycloak,
                    onCheckedChange = { viewModel.toggleHttpsKeycloak(it) }
                )
            }
            
            if (state.useHttpsKeycloak) {
                Text(
                    text = "ℹ️ Using HTTPS Keycloak (https://localhost:7001). Make sure Docker SSL environment is running.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            

            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.startOAuthFlow() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isExecuting && state.discoveryUrl.isNotBlank() && state.clientId.isNotBlank()
                ) {
                    if (state.isExecuting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (state.isExecuting) "Processing..." else "Start OAuth Flow")
                }
                
                OutlinedButton(
                    onClick = { viewModel.resetFlow() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isExecuting
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset")
                }
            }

            HorizontalDivider()

            StatusInfoCard(
                title = "Flow Status",
                statusItems = buildList {
                    add("Status" to state.flowStatus)
                    add("Redirect URL" to if (state.useHttps) "https://localhost:8443/redirect" else "http://localhost:8080/redirect")
                    if (state.serverDetails.isNotBlank()) {
                        add("Server Config" to state.serverDetails)
                    }
                }
            )
            
            if (state.accessToken.isNotBlank()) {
                OutlinedCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Access Token:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = state.accessToken.take(100) + if (state.accessToken.length > 100) "..." else "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp),
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            DebugLogsSection(
                logs = debugLogs,
                maxHeight = 300
            )
        }
    }
}
