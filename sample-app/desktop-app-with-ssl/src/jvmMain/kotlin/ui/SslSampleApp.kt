package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import ui.components.StatusHeader
import ui.screens.SslConfigurationScreen
import ui.screens.OAuthFlowScreen
import ui.screens.ReferenceScreen
import ui.theme.SslSampleTheme

data class TabItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val content: @Composable () -> Unit
)

@OptIn(ExperimentalOpenIdConnect::class, ExperimentalMaterial3Api::class)
@Composable
fun SslSampleApp() {
    var selectedTab by remember { mutableStateOf(0) }
    
    val tabs = remember {
        listOf(
            TabItem("SSL Configuration", Icons.Default.Security) { SslConfigurationScreen() },
            TabItem("OAuth Flow Demo", Icons.AutoMirrored.Filled.Login) { OAuthFlowScreen() },
            TabItem("Reference", Icons.AutoMirrored.Filled.MenuBook) { ReferenceScreen() }
        )
    }
    
    SslSampleTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "OIDC SSL Sample Application",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StatusHeader()
                
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(tab.title) },
                            icon = { Icon(tab.icon, contentDescription = null) }
                        )
                    }
                }
                
                tabs[selectedTab].content()
            }
        }
    }
}