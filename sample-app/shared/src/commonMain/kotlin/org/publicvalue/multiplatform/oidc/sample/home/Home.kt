package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.publicvalue.multiplatform.oidc.sample.domain.TokenData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    state: HomeUiState,
    modifier: Modifier = Modifier
) {
    var greetingText by remember { mutableStateOf("Hello, World!") }
    var showImage by remember { mutableStateOf(false) }

//    val settingsStore = LocalSettingsStore.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OIDC Demo") },
                actions = {
                    IconButton(onClick = { state.eventSink(HomeUiEvent.NavigateToConfig) }) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                    }
                }
            )
        }
    ) {
        Home(
            modifier = modifier.padding(it),
            loginEnabled = state.loginEnabled,
            logoutEnabled = state.logoutEnabled,
            refreshEnabled = state.refreshEnabled,
            onLoginClick = { state.eventSink(HomeUiEvent.Login) },
            onLogoutClick = { state.eventSink(HomeUiEvent.Logout(it)) },
            onRefreshClick = { state.eventSink(HomeUiEvent.Refresh) },
            tokenData = state.tokenData,
            subject = state.subject,
            errorMessage = state.errorMessage
        )
    }
}

@Composable
fun Home(
    modifier: Modifier = Modifier,
    loginEnabled: Boolean,
    refreshEnabled: Boolean,
    logoutEnabled: Boolean,
    onLoginClick: () -> Unit,
    onLogoutClick: (Boolean) -> Unit,
    onRefreshClick: () -> Unit,
    tokenData: TokenData?,
    subject: String?,
    errorMessage: String?
) {
    var useWebFlow by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row {
            Text("Token Data:")
            if (tokenData == null) {
                Text("No token")
            }
        }
        tokenData?.let {
            Row() {
                Text("Access token:")
                Text(it.accessToken ?: "")
            }
        }
        tokenData?.let {
            Row() {
                Text("Token lifetime:")
                Text("${it.expiresIn}")
            }
        }
        tokenData?.let { Row() {
            Text("Refresh token:")
            Text(it.refreshToken ?: "") }
        }
        subject?.let { Row() {
            Text("Subject:")
            Text(subject) }
        }
        errorMessage?.let {
            Text("Error: $it")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    onLoginClick()
                },
                enabled = loginEnabled
            ) {
                Text("Login")
            }

            Button(onClick = {
                onLogoutClick(useWebFlow)
            },
                enabled = logoutEnabled) {
                Text("Logout")
            }

            Button(onClick = {
                onRefreshClick()
            },
                enabled = refreshEnabled) {
                Text("Refresh")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Switch(useWebFlow, onCheckedChange = {useWebFlow = it})
            Text("Use GET request with post_endsession_redirect_uri for logout")
        }
    }
}