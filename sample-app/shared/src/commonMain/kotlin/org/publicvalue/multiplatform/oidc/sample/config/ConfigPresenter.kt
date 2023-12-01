package org.publicvalue.multiplatform.oidc.sample.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.Navigator
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.discovery.OpenIDConnectDiscover
import org.publicvalue.multiplatform.oidc.sample.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oidc.sample.circuit.catchErrorMessage
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.domain.ClientSettings
import org.publicvalue.multiplatform.oidc.sample.domain.IdpSettings
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

class ConfigPresenter(
    val navigator: Navigator
): ErrorPresenter<ConfigUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

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

                is ConfigUiEvent.ChangeClientProperty<*> -> {
                    val clientSettings = clientSettings ?: ClientSettings.Empty

                    when (event.prop) {

                        ClientSettings::client_id -> {
                            scope.launch {
                                settingsStore.setClientSettings(clientSettings.copy(client_id = event.value as String))
                            }
                        }

                        ClientSettings::client_secret -> {
                            scope.launch {
                                settingsStore.setClientSettings(clientSettings.copy(client_secret = event.value as String))
                            }
                        }

                        ClientSettings::scope -> {
                            scope.launch {
                                settingsStore.setClientSettings(clientSettings.copy(scope = event.value as String))
                            }
                        }

                        ClientSettings::code_challenge_method -> {
                            scope.launch {
                                settingsStore.setClientSettings(clientSettings.copy(code_challenge_method = event.value as CodeChallengeMethod))
                            }
                        }
                    }
                }
                is ConfigUiEvent.ChangeIdpProperty<*> -> {
                    val idpSettings = idpSettings ?: IdpSettings.Empty

                    when (event.prop) {
                        IdpSettings::discoveryUrl -> scope.launch {
                            settingsStore.setIdpSettings(idpSettings.copy(discoveryUrl = event.value as String))
                        }
                        IdpSettings::endpointToken -> scope.launch {
                            settingsStore.setIdpSettings(idpSettings.copy(endpointToken = event.value as String))
                        }
                        IdpSettings::endpointAuthorization -> scope.launch {
                            settingsStore.setIdpSettings(idpSettings.copy(endpointAuthorization = event.value as String))
                        }
                        IdpSettings::endpointEndSession -> scope.launch {
                            settingsStore.setIdpSettings(idpSettings.copy(endpointEndSession = event.value as String))
                        }
                    }
                }
                ConfigUiEvent.Discover -> {
                    scope.launch {
                        catchErrorMessage {
                            if (!idpSettings?.discoveryUrl.isNullOrEmpty() && idpSettings?.discoveryUrl?.let { Url(it) } != null) {
                                settingsStore.setIdpSettings(
                                    IdpSettings(
                                        discoveryUrl = idpSettings?.discoveryUrl
                                    )
                                )
                                val d = OpenIDConnectDiscover()
                                idpSettings?.let { idpSettings ->
                                    idpSettings.discoveryUrl?.let {
                                        val config = d.downloadConfiguration(it)
                                        val newSettings = idpSettings.copy(
                                            endpointToken = config.token_endpoint,
                                            endpointAuthorization = config.authorization_endpoint,
                                            endpointDeviceAuthorization = config.device_authorization_endpoint,
                                            endpointEndSession = config.end_session_endpoint,
                                            endpointIntrospection = config.introspection_endpoint,
                                            endpointUserInfo = config.userinfo_endpoint
                                        )
                                        settingsStore.setIdpSettings(newSettings)
                                    }
                                }
                            }
                        }
                    }
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