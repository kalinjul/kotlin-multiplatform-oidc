package org.publicvalue.multiplatform.oauth.idplist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.components.OidcPlaygroundTopBar
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.screens.IdpListScreen

@Inject
class IdpListUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is IdpListScreen -> {
            ui<IdpListUiState> { state, modifier ->
                IdpList(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun IdpList(
    state: IdpListUiState,
    modifier: Modifier = Modifier,
) {
    IdpList(
        modifier = modifier,
        idps = state.idps,
        onIdpClick = {
            state.eventSink(IdpListUiEvent.NavigateToIdp(it))
        },
        onAddIdpClick = {
            state.eventSink(IdpListUiEvent.AddIdp)
        },
        onRemoveIdpClick = {
            state.eventSink(IdpListUiEvent.RemoveIdp(it))
        }
    )
}

@Composable
internal fun IdpList(
    modifier: Modifier = Modifier,
    idps: List<Identityprovider>,
    onIdpClick: (Identityprovider) -> Unit,
    onAddIdpClick: () -> Unit,
    onRemoveIdpClick: (Identityprovider) -> Unit
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            OidcPlaygroundTopBar(
                title = { Text("Identity Providers") },
                isRootScreen = true,
                actions = {
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddIdpClick()
                },
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Provider")
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) {
        LazyColumn(
            modifier = Modifier.padding(it)
                .padding(16.dp)
        ) {
            itemsIndexed(idps) { index, idp ->
                Column(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth()
                            .clickable { onIdpClick(idp) },
                        border = BorderStroke(1.dp, color = colorScheme.primaryContainer),
                    ) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Column(
                                modifier = Modifier
                                .padding(16.dp)
                                .weight(1f)
                            ) {
                                Text(idp.name)
                                Text("Clients: 0")
                            }
                            Column {
                                TextButton(onClick = { onRemoveIdpClick(idp) }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}