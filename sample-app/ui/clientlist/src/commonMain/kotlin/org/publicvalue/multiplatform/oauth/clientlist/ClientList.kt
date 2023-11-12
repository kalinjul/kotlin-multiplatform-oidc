package org.publicvalue.multiplatform.oauth.ClientList

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.publicvalue.multiplatform.oauth.screens.ClientListScreen
import me.tatarka.inject.annotations.Inject
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
        clients = state.clients,
        onClientClick = {
            state.eventSink(ClientListUiEvent.NavigateToClientDetail(it))
        }
    )
}

@Composable
internal fun ClientList(
    modifier: Modifier = Modifier,
    clients: List<String>,
    onClientClick: (String) -> Unit
) {
    Text("Client list")
//    LazyColumn(modifier = Modifier.padding(16.dp)) {
//        items(idps) {
//            Surface(color = colorScheme.secondaryContainer, shape = shapes.extraLarge) {
//                Button(onClick = { onIdpClick(it)}) {
//                    Text(it.name)
//                }
//            }
//        }
//    }
}