package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.components.ColumnHeadline
import org.publicvalue.multiplatform.oauth.compose.components.FormHeadline
import org.publicvalue.multiplatform.oauth.compose.components.OidcPlaygroundTopBar
import org.publicvalue.multiplatform.oauth.compose.components.SingleLineInput
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oauth.domain.Constants

@Inject
class ClientDetailUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is ClientDetailScreen -> {
            ui<ClientDetailUiState> { state, modifier ->
                ClientDetail(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun ClientDetail(
    state: ClientDetailUiState,
    modifier: Modifier = Modifier,
) {
    ClientDetail(
        modifier = modifier,
        client = state.client,
        onNavigateUp = {
            state.eventSink(ClientDetailUiEvent.NavigateUp)
        },
        onNameChange = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::name, it))
        },
        onClientIdChange = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::client_id, it))
        },
        onClientSecretChange =  {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::client_secret, it))
        },
        onCodeChallengeMethodClick = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::code_challenge_method, it))
        },
        onScopeChange = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::scope, it))
        },
        onLogin = {
            state.eventSink(ClientDetailUiEvent.Login)
        }
    )
}

@Composable
internal fun ClientDetail(
    modifier: Modifier = Modifier,
    client: Client?,
    onNavigateUp: () -> Unit,
    onNameChange: (String) -> Unit,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onScopeChange: (String) -> Unit,
    onCodeChallengeMethodClick: (CodeChallengeMethod) -> Unit,
    onLogin: () -> Unit
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            OidcPlaygroundTopBar(
                title = { Text("Client: ${client?.name.orEmpty()}") },
                isRootScreen = false,
                actions = {
                },
                navigateUp = onNavigateUp
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) {
        Row(Modifier.padding(it).padding(16.dp)) {
            Column(Modifier.padding(16.dp).weight(1f)) {
                ColumnHeadline(text = "Client Configuration")
                ClientDetail(
                    client = client,
                    onNameChange = onNameChange,
                    onClientIdChange = onClientIdChange,
                    onClientSecretChange = onClientSecretChange,
                    onScopeChange = onScopeChange,
                    onCodeChallengeMethodClick = onCodeChallengeMethodClick
                )
            }
            Column(Modifier.padding(16.dp).weight(1f)) {
                ColumnHeadline(text = "Auth Flow")
                AuthFlow(
                    onLogin = onLogin
                )
            }
        }
    }
}

@Composable
internal fun ClientDetail(
    modifier: Modifier = Modifier,
    client: Client?,
    onNameChange: (String) -> Unit,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onScopeChange: (String) -> Unit,
    onCodeChallengeMethodClick: (CodeChallengeMethod) -> Unit,
) {
    var name by remember(client?.name != null) {
        mutableStateOf(client?.name.orEmpty())
    }

    var clientId by remember(client?.client_id != null) {
        mutableStateOf(client?.client_id.orEmpty())
    }

    var clientSecret by remember(client?.client_secret != null) {
        mutableStateOf(client?.client_secret.orEmpty())
    }

    var scope by remember(client?.scope != null) {
        mutableStateOf(client?.scope.orEmpty())
    }

    Column(modifier = modifier) {
        FormHeadline(text = "Client")
        SingleLineInput(
            value = name,
            onValueChange = { name = it; onNameChange(it) },
            label = { Text("Name") }
        )
        SingleLineInput(
            value = clientId,
            onValueChange = { clientId = it; onClientIdChange(it)},
            label = { Text("Client ID") }
        )
        SingleLineInput(
            value = clientSecret,
            onValueChange = { clientSecret = it; onClientSecretChange(it)},
            label = { Text("Client secret") }
        )
        FormHeadline(text = "Auth code flow request parameters")
        SingleLineInput(
            value = scope,
            onValueChange = { scope = it; onScopeChange(it)},
            label = { Text("Scope") }
        )
        FormHeadline(text = "Code challenge method")

        CodeChallengeMethod.entries.forEach {
            Row() {
                RadioButton(client?.code_challenge_method == it, onClick = { onCodeChallengeMethodClick(it) })
                Text(modifier = Modifier.padding(start = 16.dp), text = it.name)
            }
        }
//        Row() {
//            RadioButton(false, onClick = null)
//            Text(modifier = Modifier.padding(start = 16.dp), text = "S256")
//        }
//        Row() {
//            RadioButton(false, onClick = null)
//            Text(modifier = Modifier.padding(start = 16.dp), text = "plain")
//        }
//        Row() {
//            RadioButton(false, onClick = null)
//            Text(modifier = Modifier.padding(start = 16.dp), text = "off")
//        }
        Text("Note: redirect_url will always be ${Constants.REDIRECT_URL}")
    }
}

@Composable
internal fun AuthFlow(
    modifier: Modifier = Modifier,
    onLogin: () -> Unit
) {
    Column(modifier = modifier) {
        Button(onClick = { onLogin() }) {
            Text("Login")
        }
        FormHeadline(text = "Discovery")
        ExpandableInfo(
            label = "Request (length: )",
            text = "Much more information \n including newlines"
        )
        FormHeadline(text = "Auth code")
        FormHeadline(text = "Token exchange")
        FormHeadline(text = "Access Token")
        FormHeadline(text = "Refresh Token")
        FormHeadline(text = "Id Token")
    }
}

@Composable
fun ExpandableInfo(
    modifier: Modifier = Modifier,
    label: String,
    text: String
) {
    var expanded by remember() { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label)
            IconButton(onClick = { expanded = !expanded }) {
                if (expanded) {
                    Icon(imageVector = Icons.Default.ExpandLess, contentDescription = "show less")
                } else {
                    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = "show more")
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Surface(
                color = colorScheme.primaryContainer
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = text,
                    style = typography.bodyMedium
                )
            }
        }
    }
}
