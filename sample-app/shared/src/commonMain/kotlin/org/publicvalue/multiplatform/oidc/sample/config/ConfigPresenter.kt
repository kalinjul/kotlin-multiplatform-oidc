package org.publicvalue.multiplatform.oidc.sample.config

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.Navigator
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.discovery.OpenIdConnectDiscover
import org.publicvalue.multiplatform.oidc.sample.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oidc.sample.circuit.catchErrorMessage
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.domain.ClientSettings
import org.publicvalue.multiplatform.oidc.sample.domain.IdpSettings
import kotlin.String

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
                is ConfigUiEvent.ChangeClientId -> {
                    val clientSettings = clientSettings ?: ClientSettings.Empty

                    scope.launch {
                        settingsStore.setClientSettings(clientSettings.copy(client_id = event.clientId))
                    }
                }
                is ConfigUiEvent.ChangeClientSecret -> {
                    val clientSettings = clientSettings ?: ClientSettings.Empty

                    scope.launch {
                        settingsStore.setClientSettings(clientSettings.copy(client_secret = event.clientSecret))
                    }
                }
                is ConfigUiEvent.ChangeScope -> {
                    val clientSettings = clientSettings ?: ClientSettings.Empty

                    scope.launch {
                        settingsStore.setClientSettings(clientSettings.copy(scope = event.scope))
                    }
                }
                is ConfigUiEvent.ChangeCodeChallengeMethod -> {
                    val clientSettings = clientSettings ?: ClientSettings.Empty

                    scope.launch {
                        settingsStore.setClientSettings(clientSettings.copy(code_challenge_method = event.codeChallengeMethod))
                    }
                }
                is ConfigUiEvent.ChangeDiscoveryUrl -> {
                    val idpSettings = idpSettings ?: IdpSettings.Empty

                    scope.launch {
                        settingsStore.setIdpSettings(idpSettings.copy(discoveryUrl = event.discoveryUrl))
                    }
                }
                is ConfigUiEvent.ChangeEndpointAuthorization -> {
                    val idpSettings = idpSettings ?: IdpSettings.Empty

                    scope.launch {
                        settingsStore.setIdpSettings(idpSettings.copy(endpointAuthorization = event.endpointAuthorization))
                    }
                }
                is ConfigUiEvent.ChangeEndpointEndSession -> {
                    val idpSettings = idpSettings ?: IdpSettings.Empty

                    scope.launch {
                        settingsStore.setIdpSettings(idpSettings.copy(endpointEndSession = event.endpointEndSession))
                    }
                }
                is ConfigUiEvent.ChangeEndpointToken -> {
                    val idpSettings = idpSettings ?: IdpSettings.Empty

                    scope.launch {
                        settingsStore.setIdpSettings(idpSettings.copy(endpointToken = event.endpointToken))
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
                                val d = OpenIdConnectDiscover()
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