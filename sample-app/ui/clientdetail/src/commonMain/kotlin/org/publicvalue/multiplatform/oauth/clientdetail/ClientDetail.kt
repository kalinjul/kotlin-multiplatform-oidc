package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.components.OidcPlaygroundTopBar
import org.publicvalue.multiplatform.oauth.data.db.Client

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
        }
    )
}

@Composable
internal fun ClientDetail(
    modifier: Modifier = Modifier,
    client: Client?,
    onNavigateUp: () -> Unit,
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
        Row(modifier.padding(it).padding(16.dp)) {
            Text("Client detail")
        }
    }
}