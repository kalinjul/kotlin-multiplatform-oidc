package org.publicvalue.multiplatform.oauth.ClientDetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider

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
        client = "C"
    )
}

@Composable
internal fun ClientDetail(
    modifier: Modifier = Modifier,
    client: String,
) {
    Text("Client detail")
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