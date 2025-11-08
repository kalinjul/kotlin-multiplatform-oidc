package org.publicvalue.multiplatform.oidc.sample.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.publicvalue.multiplatform.oidc.sample.PlatformConstants
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Config(
    state: ConfigUiState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OIDC Demo") },
                navigationIcon = {
                    IconButton(onClick = { state.eventSink(ConfigUiEvent.NavigateBack) }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                }
            )
        }
    ) {
        Config(
            modifier = Modifier.padding(it),
            discoveryUrl = state.idpSettings.discoveryUrl,
            authEndpoint = state.idpSettings.endpointAuthorization,
            tokenEndpoint = state.idpSettings.endpointToken,
            endSessionEndpoint = state.idpSettings.endpointEndSession,
            clientId = state.clientSettings.clientId,
            clientSecret = state.clientSettings.clientSecret,
            scope = state.clientSettings.scope,
            challengeMethod = state.clientSettings.codeChallengeMethod,
            onChangeAuthEndpoint = {
                state.eventSink(
                    ConfigUiEvent.ChangeEndpointAuthorization(it)
                )
            },
            onChangeDiscoveryUrl = {
                state.eventSink(
                    ConfigUiEvent.ChangeDiscoveryUrl(it)
                )
            },
            onChangeTokenEndpoint = {
                state.eventSink(
                    ConfigUiEvent.ChangeEndpointToken(it)
                )
            },
            onChangeEndSessionEndpoint = {
                state.eventSink(
                    ConfigUiEvent.ChangeEndpointEndSession(it)
                )
            },
            onChangeClientId = {
                state.eventSink(
                    ConfigUiEvent.ChangeClientId(it)
                )
            },
            onChangeClientSecret = {
                state.eventSink(
                    ConfigUiEvent.ChangeClientSecret(it)
                )
            },
            onChangeScope = {
                state.eventSink(
                    ConfigUiEvent.ChangeScope(it)
                )
            },
            onChangeChallengeMethod = {
                state.eventSink(
                    ConfigUiEvent.ChangeCodeChallengeMethod(it)
                )
            },
            onClickDiscover = {
                state.eventSink(
                    ConfigUiEvent.Discover
                )
            }
        )
    }
}

@Composable
internal fun Config(
    modifier: Modifier = Modifier,
    discoveryUrl: String?,
    authEndpoint: String?,
    tokenEndpoint: String?,
    endSessionEndpoint: String?,
    clientId: String?,
    clientSecret: String?,
    scope: String?,
    challengeMethod: CodeChallengeMethod?,
    onChangeDiscoveryUrl: (String) -> Unit,
    onChangeAuthEndpoint: (String) -> Unit,
    onChangeTokenEndpoint: (String) -> Unit,
    onChangeEndSessionEndpoint: (String) -> Unit,
    onChangeClientId: (String) -> Unit,
    onChangeClientSecret: (String) -> Unit,
    onChangeScope: (String) -> Unit,
    onChangeChallengeMethod: (CodeChallengeMethod) -> Unit,
    onClickDiscover: () -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var discoveryUrl by remember(discoveryUrl == null) {
            mutableStateOf(discoveryUrl.orEmpty())
        }
        var authEndpoint by remember(authEndpoint == null) {
            mutableStateOf(authEndpoint.orEmpty())
        }
        var tokenEndpoint by remember(tokenEndpoint == null) {
            mutableStateOf(tokenEndpoint.orEmpty())
        }
        var endSessionEndpoint by remember(endSessionEndpoint == null) {
            mutableStateOf(endSessionEndpoint.orEmpty())
        }

        var clientId by remember(clientId != null) {
            mutableStateOf(clientId.orEmpty())
        }

        var clientSecret by remember(clientSecret != null) {
            mutableStateOf(clientSecret.orEmpty())
        }

        var scope by remember(scope != null) {
            mutableStateOf(scope.orEmpty())
        }

        FormHeadline(text = "IDP")
        SingleLineInput(
            value = discoveryUrl,
            onValueChange = {
                discoveryUrl = it
                onChangeDiscoveryUrl(it)
            },
            label = { Text("Discovery URL") }
        )
        TextButton(onClick = { onClickDiscover() }) {
            Text("Discover")
        }
        FormHeadline(text = "Endpoints")
        SingleLineInput(
            value = authEndpoint,
            onValueChange = {
                authEndpoint = it
                onChangeAuthEndpoint(it)
            },
            label = { Text("Authorization") }
        )
        SingleLineInput(
            value = tokenEndpoint,
            onValueChange = {
                tokenEndpoint = it
                onChangeTokenEndpoint(it)
            },
            label = { Text("Token") }
        )
        SingleLineInput(
            value = endSessionEndpoint,
            onValueChange = {
                endSessionEndpoint = it
                onChangeEndSessionEndpoint(it)
            },
            label = { Text("End Session") }
        )

        FormHeadline(text = "Client")
        SingleLineInput(
            value = clientId,
            onValueChange = {
                clientId = it
                onChangeClientId(it)
            },
            label = { Text("Client ID") }
        )
        SingleLineInput(
            value = clientSecret,
            onValueChange = {
                clientSecret = it
                onChangeClientSecret(it)
            },
            label = { Text("Client secret") }
        )
        FormHeadline(text = "Auth code flow request parameters")
        SingleLineInput(
            value = scope,
            onValueChange = {
                scope = it
                onChangeScope(it)
            },
            label = { Text("Scope") }
        )
        FormHeadline(text = "Code challenge method")

        CodeChallengeMethod.entries.forEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                RadioButton(challengeMethod == it, onClick = { onChangeChallengeMethod(it) })
                Text(modifier = Modifier.padding(start = 16.dp), text = it.name)
            }
        }
        Text("Note: redirect_url is ${PlatformConstants.redirectUrl}")
        Text("Note: post_logout_redirect_url is ${PlatformConstants.postLogoutRedirectUrl}")
    }
}
