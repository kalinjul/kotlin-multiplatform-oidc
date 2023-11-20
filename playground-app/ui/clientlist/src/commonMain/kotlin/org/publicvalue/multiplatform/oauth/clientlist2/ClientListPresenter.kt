package org.publicvalue.multiplatform.oauth.clientlist2

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.retained.collectAsRetainedState
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
import org.publicvalue.multiplatform.oauth.compose.circuit.catchErrorMessage
import org.publicvalue.multiplatform.oauth.data.daos.ClientDao
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import org.publicvalue.multiplatform.oauth.domain.AddClient
import org.publicvalue.multiplatform.oauth.domain.DiscoverIdpConfig
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen

@Inject
class ClientListUiPresenterFactory(
    private val presenterFactory: (Navigator, ClientListScreen) -> ClientListPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ClientListScreen -> presenterFactory(navigator, screen)
            else -> null
        }
    }
}

@Inject
class ClientListPresenter(
    @Assisted private val navigator: Navigator,
    @Assisted private val screen: ClientListScreen,
    private val logger: Logger,
    private val addClient: AddClient,
    private val discover: DiscoverIdpConfig,
    private val clientDao: ClientDao,
    private val idpDao: IdpDao
) : ErrorPresenter<ClientListUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

    @Composable
    override fun present(): ClientListUiState {
        val scope = rememberCoroutineScope()

        val clients by clientDao.getClients(screen.idpId).collectAsState(listOf())
        val idp by idpDao.getIdp(screen.idpId).collectAsRetainedState(null)

        val errorMessage by this.errorMessage.collectAsRetainedState()

        fun eventSink(event: ClientListUiEvent) {
            when (event) {
                is ClientListUiEvent.NavigateToClientDetail -> navigator.goTo(ClientDetailScreen(event.client.id))
                ClientListUiEvent.AddClient -> {
                    scope.launch {
                        idp?.let {
                            addClient(idp = it)
                        }
                    }
                }

                ClientListUiEvent.NavigateUp -> {
                    navigator.pop()
                }

                is ClientListUiEvent.RemoveClient -> {
                    scope.launch {
                        clientDao.deleteEntity(event.client)
                    }
                }

                is ClientListUiEvent.ChangeIdpProperty<*> -> {
                    idp?.let {  idp ->
                        scope.launch {
                            when (event.prop) {
                                Identityprovider::name -> idpDao.update(idp.copy(name = event.value as String))
                                Identityprovider::discoveryUrl -> idpDao.update(idp.copy(discoveryUrl = event.value as String))
                                Identityprovider::endpointToken -> idpDao.update(idp.copy(endpointToken = event.value as String))
                                Identityprovider::endpointAuthorization -> idpDao.update(idp.copy(endpointAuthorization = event.value as String))
                                Identityprovider::useDiscovery -> idpDao.update(idp.copy(useDiscovery = event.value as Boolean))
                            }
                        }
                    }
                }

                ClientListUiEvent.Discover -> {
                    scope.launch {
                        idp?.let {
                            catchErrorMessage {
                                resetErrorMessage()
                                discover(idp = it)
                            }
                        }
                    }
                }

                ClientListUiEvent.ResetErrorMessage -> {
                    resetErrorMessage()
                }
            }
        }

//        println("${errorMessage}")

        return ClientListUiState(
            errorMessage = errorMessage,
            isLoading = false,
            eventSink = ::eventSink,
            clients = clients,
            idp = idp,
        )
    }
}