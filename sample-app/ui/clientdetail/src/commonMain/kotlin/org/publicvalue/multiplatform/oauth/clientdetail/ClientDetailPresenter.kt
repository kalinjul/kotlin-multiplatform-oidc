package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oauth.data.daos.ClientDao
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import org.publicvalue.multiplatform.oidc.discovery.Discover

@Inject
class ClientDetailUiPresenterFactory(
    private val presenterFactory: (Navigator, ClientDetailScreen) -> ClientDetailPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ClientDetailScreen -> presenterFactory(navigator, screen)
            else -> null
        }
    }
}

@Inject
class ClientDetailPresenter(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: ClientDetailScreen,
    private val logger: Logger,
    private val clientDao: ClientDao
) : ErrorPresenter<ClientDetailUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

    @Composable
    override fun present(): ClientDetailUiState {
        val scope = rememberCoroutineScope()

        val client by clientDao.getClient(screen.clientId).collectAsState(null)

        fun eventSink(event: ClientDetailUiEvent) {
            when (event) {
                ClientDetailUiEvent.Call -> {
                    scope.launch {
                        val discover = Discover()
                        val config = discover.downloadConfiguration("https://")
                        println(config)
                    }
                }

                ClientDetailUiEvent.NavigateUp -> {
                    navigator.pop()
                }
            }
        }

        return ClientDetailUiState(
            errorMessage = null,
            isLoading = false,
            eventSink = ::eventSink,
            client = client
        )
    }
}
