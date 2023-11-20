package org.publicvalue.multiplatform.oidc.sample.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import org.publicvalue.multiplatform.oidc.sample.data.ClientSettings
import org.publicvalue.multiplatform.oidc.sample.data.IdpSettings
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore

class ConfigPresenter(
    val navigator: Navigator
): Presenter<ConfigUiState> {
    @Composable
    override fun present(): ConfigUiState {
        val scope = rememberCoroutineScope()
        val settingsStore = LocalSettingsStore.current

        val clientSettings by settingsStore.observeClientSettings().collectAsRetainedState()
        val idpSettings by settingsStore.observeIdpSettings().collectAsRetainedState()

        fun eventSink(event: ConfigUiEvent) {
            when(event) {
                ConfigUiEvent.NavigateBack -> {
                     navigator.pop()
                }
            }
        }

        return ConfigUiState(
            idpSettings = idpSettings ?: IdpSettings.Empty,
            clientSettings = clientSettings ?: ClientSettings.Empty,
            eventSink = ::eventSink
        )
    }
}