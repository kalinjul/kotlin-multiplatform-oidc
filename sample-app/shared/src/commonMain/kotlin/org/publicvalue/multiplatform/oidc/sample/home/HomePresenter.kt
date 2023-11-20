package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.domain.TokenData
import org.publicvalue.multiplatform.oidc.sample.screens.ConfigScreen

class HomePresenter(
    val navigator: Navigator
): Presenter<HomeUiState> {
    @Composable
    override fun present(): HomeUiState {
        val scope = rememberCoroutineScope()
        val settingsStore = LocalSettingsStore.current

        val clientSettings = settingsStore.observeClientSettings().collectAsRetainedState()
        val idpSettings = settingsStore.observeIdpSettings().collectAsRetainedState()

        fun eventSink(event: HomeUiEvent) {
            when(event) {
                HomeUiEvent.Login -> TODO()
                HomeUiEvent.Logout -> TODO()
                HomeUiEvent.NavigateToConfig -> {
                    navigator.goTo(ConfigScreen)
                }
                HomeUiEvent.Refresh -> TODO()
            }
        }

        return HomeUiState(
            loginEnabled = false,
            refreshEnabled = false,
            logoutEnabled = false,
            tokenData = TokenData(
                token = "",
                refreshToken = "",
                tokenLifetime = ""
            ),
            eventSink = ::eventSink
        )
    }
}