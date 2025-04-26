package org.publicvalue.multiplatform.oauth.clientlist2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.publicvalue.multiplatform.oauth.screens.ClientListScreen
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.components.ColumnHeadline
import org.publicvalue.multiplatform.oauth.compose.components.ErrorMessageBox
import org.publicvalue.multiplatform.oauth.compose.components.FormHeadline
import org.publicvalue.multiplatform.oauth.compose.components.OidcPlaygroundTopBar
import org.publicvalue.multiplatform.oauth.compose.components.SingleLineInput
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider

@Inject
class ClientListUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is ClientListScreen -> {
            ui<ClientListUiState> { state, modifier ->
                ClientList(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun ClientList(
    state: ClientListUiState,
    modifier: Modifier = Modifier,
) {
    ClientList(
        modifier = modifier,
        errorMessage = state.errorMessage,
        resetErrorMessage = {
            state.eventSink(ClientListUiEvent.ResetErrorMessage)
        },
        idp = state.idp,
        clients = state.clients,
        onNavigateUp = {
            state.eventSink(ClientListUiEvent.NavigateUp)
        },
        onClientClick = {
            state.eventSink(ClientListUiEvent.NavigateToClientDetail(it))
        },
        onAddClientClick = {
            state.eventSink(ClientListUiEvent.AddClient)
        },
        onRemoveClientClick = {
            state.eventSink(ClientListUiEvent.RemoveClient(it))
        },
        onNameChange = {
            state.eventSink(ClientListUiEvent.ChangeIdpProperty(Identityprovider::name, it))
        },
        onUseDiscoveryChange = {
            state.eventSink(ClientListUiEvent.ChangeIdpProperty(Identityprovider::useDiscovery, it))
        },
        onDiscoveryUrlChange = {
            state.eventSink(ClientListUiEvent.ChangeIdpProperty(Identityprovider::discoveryUrl, it))
        },
        onDiscoverClick = {
            state.eventSink(ClientListUiEvent.Discover)
        },
        onEndpointTokenChange = {
            state.eventSink(ClientListUiEvent.ChangeIdpProperty(Identityprovider::endpointToken, it))
        },
        onEndpointAuthorizationChange = {
            state.eventSink(ClientListUiEvent.ChangeIdpProperty(Identityprovider::endpointAuthorization, it))
        },
        onEndpointEndsessionChange = {
            state.eventSink(ClientListUiEvent.ChangeIdpProperty(Identityprovider::endpointEndSession, it))
        }
    )
}

@Composable
internal fun ClientList(
    modifier: Modifier = Modifier,
    errorMessage: String?,
    resetErrorMessage: () -> Unit,
    idp: Identityprovider?,
    clients: List<Client>,
    onNavigateUp: () -> Unit,
    onClientClick: (Client) -> Unit,
    onAddClientClick: () -> Unit,
    onRemoveClientClick: (Client) -> Unit,
    onEndpointTokenChange: (String) -> Unit,
    onEndpointAuthorizationChange: (String) -> Unit,
    onEndpointEndsessionChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onUseDiscoveryChange: (Boolean) -> Unit,
    onDiscoveryUrlChange: (String) -> Unit,
    onDiscoverClick: () -> Unit,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            OidcPlaygroundTopBar(
                title = { Text("IDP: ${idp?.name}") },
                isRootScreen = false,
                actions = {
                },
                navigateUp = onNavigateUp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddClientClick()
                },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Provider")
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) {
        Box(modifier.padding(it)) {
            Row() {
                Column(Modifier.padding(16.dp).weight(1f)) {
                    ColumnHeadline("IDP Configuration")
                    IdpDetail(
                        modifier = Modifier,
                        idp = idp,
                        onEndpointTokenChange = onEndpointTokenChange,
                        onEndpointAuthorizationChange = onEndpointAuthorizationChange,
                        onEndpointEndsessionChange = onEndpointEndsessionChange,
                        onNameChange = onNameChange,
                        onUseDiscoveryChange = onUseDiscoveryChange,
                        onDiscoveryUrlChange = onDiscoveryUrlChange,
                        onDiscoverClick = onDiscoverClick
                    )
                }
                Column(Modifier.padding(16.dp).weight(1f)) {
                    ColumnHeadline("Clients")
                    ClientList(
                        modifier = Modifier,
                        clients = clients,
                        onClientClick = onClientClick,
                        onRemoveClientClick = onRemoveClientClick
                    )
                }
            }
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                ErrorMessageBox(resetErrorMessage, errorMessage)
            }
        }

    }
}

@Composable
internal fun IdpDetail(
    modifier: Modifier,
    idp: Identityprovider?,
    onEndpointTokenChange: (String) -> Unit,
    onEndpointAuthorizationChange: (String) -> Unit,
    onEndpointEndsessionChange: (String) -> Unit,
    onNameChange: (String) -> Unit,
    onUseDiscoveryChange: (Boolean) -> Unit,
    onDiscoveryUrlChange: (String) -> Unit,
    onDiscoverClick: () -> Unit,
) {
    Column(modifier = modifier) {

        var name by remember(idp?.name == null) {
            mutableStateOf(idp?.name.orEmpty())
        }
        var useDiscovery by remember(idp?.useDiscovery == null) {
            mutableStateOf(idp?.useDiscovery ?: true)
        }
        var discoveryUrl by remember(idp?.discoveryUrl == null) {
            mutableStateOf(idp?.discoveryUrl.orEmpty())
        }
        var endpointToken by remember(idp?.endpointToken == null) {
            mutableStateOf(idp?.endpointToken.orEmpty())
        }
        var endpointAuthorization by remember(idp?.endpointAuthorization == null) {
            mutableStateOf(idp?.endpointAuthorization.orEmpty())
        }
        var endpointEndSession by remember(idp?.endpointEndSession == null) {
            mutableStateOf(idp?.endpointEndSession.orEmpty())
        }

        FormHeadline(text = "General")
        SingleLineInput(
            value = name,
            onValueChange = { name = it; onNameChange(it)},
            label = { Text("Name") } )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = useDiscovery, onCheckedChange = { useDiscovery = it; onUseDiscoveryChange(it)})
            Text("Use discovery")
        }
        SingleLineInput(
            value = discoveryUrl,
            onValueChange = { discoveryUrl = it; onDiscoveryUrlChange(it) },
            label = { Text("Discovery URL") }
        )
        TextButton(onClick = { onDiscoverClick() }) {
            Text("Discover")
        }
        FormHeadline(text = "Endpoints")
        SingleLineInput(
            value = endpointAuthorization,
            onValueChange = { endpointAuthorization = it; onEndpointAuthorizationChange(it)},
            label = { Text("Authorization") }
        )
        SingleLineInput(
            value = endpointToken,
            onValueChange = { endpointToken = it; onEndpointTokenChange(it)},
            label = { Text("Token") }
        )
//        SingleLineInput(
//            value = "${idp?.endpointDeviceAuthorization}",
//            onValueChange = {},
//            label = { Text("Token") }
//        )
        SingleLineInput(
            value = "${idp?.endpointEndSession}",
            onValueChange = { endpointEndSession = it; onEndpointEndsessionChange(it) },
            label = { Text("EndSession") }
        )
//        SingleLineInput(
//            value = "${idp?.endpointUserInfo}",
//            onValueChange = {},
//            label = { Text("Token") }
//        )
//        SingleLineInput(
//            value = "${idp?.endpointToken}",
//            onValueChange = {},
//            label = { Text("Token") }
//        )
    }
}

@Composable
internal fun ClientList(
    modifier: Modifier = Modifier,
    clients: List<Client>,
    onClientClick: (Client) -> Unit,
    onRemoveClientClick: (Client) -> Unit,
) {
    LazyColumn(
        modifier = modifier
    ) {
        itemsIndexed(clients) { index, client ->
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { onClientClick(client) },
                    border = BorderStroke(1.dp, color = colorScheme.primaryContainer),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(1f)
                        ) {
                            Text(client.name)
                        }
                        Column {
                            TextButton(onClick = { onRemoveClientClick(client) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}