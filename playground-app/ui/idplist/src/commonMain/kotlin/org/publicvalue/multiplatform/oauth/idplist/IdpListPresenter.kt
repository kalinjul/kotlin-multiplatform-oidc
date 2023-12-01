package org.publicvalue.multiplatform.oauth.idplist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.domain.AddIdp
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.screens.ClientListScreen
import org.publicvalue.multiplatform.oauth.screens.IdpListScreen

@Inject
class IdpListUiPresenterFactory(
    private val presenterFactory: (Navigator) -> IdpListPresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is IdpListScreen -> presenterFactory(navigator)
            else -> null
        }
    }
}

@Inject
class IdpListPresenter(
    @Assisted private val navigator: Navigator,
    private val logger: Logger,
    private val idpDao: IdpDao,
    private val addIdp: AddIdp,
) : ErrorPresenter<IdpListUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

    @Composable
    override fun present(): IdpListUiState {
        val scope = rememberCoroutineScope()

        val idps by idpDao.getIdps().collectAsRetainedState(listOf())

        fun eventSink(event: IdpListUiEvent) {
            when (event) {
                is IdpListUiEvent.NavigateToIdp -> navigator.goTo(ClientListScreen(event.idp.id))

                IdpListUiEvent.AddIdp -> {
                    scope.launch {
                        addIdp()
                    }
                }

                is IdpListUiEvent.RemoveIdp -> {
                    scope.launch {
                        idpDao.deleteEntity(event.idp)
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            if (idpDao.getIdps().first().isEmpty()) {
                addIdp()
            }
        }

        return IdpListUiState(
            errorMessage = null,
            isLoading = false,
            eventSink = ::eventSink,
            idps = idps
        )
    }
}
