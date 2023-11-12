package org.publicvalue.multiplatform.oauth.ClientList

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import org.publicvalue.multiplatform.oauth.compose.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.screens.ClientListScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import org.publicvalue.multiplatform.oidc.discovery.Discover

@Inject
class ClientListUiPresenterFactory(
    private val presenterFactory: (Navigator) -> ClientListPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ClientListScreen -> presenterFactory(navigator)
            else -> null
        }
    }
}

@Inject
class ClientListPresenter(
    @Assisted private val navigator: Navigator,
    private val logger: Logger,
) : ErrorPresenter<ClientListUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

    @Composable
    override fun present(): ClientListUiState {
        val scope = rememberCoroutineScope()

        var clients by remember { mutableStateOf(listOf<String>(
            "Test"
        )) }

        fun eventSink(event: ClientListUiEvent) {
            when (event) {
                is ClientListUiEvent.NavigateToClientDetail -> navigator.goTo(ClientDetailScreen(event.client.toLong()))
            }
        }

        return ClientListUiState(
            errorMessage = null,
            isLoading = false,
            eventSink = ::eventSink,
            clients = clients
        )
    }
}
