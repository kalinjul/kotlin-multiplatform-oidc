package org.publicvalue.multiplatform.oauth.ClientDetail

import androidx.compose.runtime.Composable
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
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import org.publicvalue.multiplatform.oidc.discovery.Discover

@Inject
class ClientDetailUiPresenterFactory(
    private val presenterFactory: (Navigator) -> ClientDetailPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ClientDetailScreen -> presenterFactory(navigator)
            else -> null
        }
    }
}

@Inject
class ClientDetailPresenter(
    @Assisted private val navigator: Navigator,
    private val logger: Logger,
) : ErrorPresenter<ClientDetailUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

    @Composable
    override fun present(): ClientDetailUiState {
        val scope = rememberCoroutineScope()

        fun eventSink(event: ClientDetailUiEvent) {
            when (event) {
                ClientDetailUiEvent.Call -> {
                    scope.launch {
                        val discover = Discover()
                        val config = discover.downloadConfiguration("https://")
                        println(config)
                    }
                }
            }
        }

        return ClientDetailUiState(
            errorMessage = null,
            isLoading = false,
            eventSink = ::eventSink,
            client = ""
        )
    }
}
