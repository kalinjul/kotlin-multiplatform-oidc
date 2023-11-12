package org.publicvalue.multiplatform.oauth.idplist

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun IdpList(
    modifier: Modifier = Modifier,
    idps: List<Identityprovider>,
    onIdpClick: (Identityprovider) -> Unit
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) {
        LazyColumn(modifier = Modifier.padding(it)
            .padding(16.dp)) {
            items(idps) {
                Surface(color = colorScheme.secondaryContainer, shape = shapes.extraLarge) {
                    Button(onClick = { onIdpClick(it)}) {
                        Text(it.name)
                    }
                }
            }
        }
    }
}