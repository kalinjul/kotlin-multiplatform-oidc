package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalUriHandler
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.AuthFlowFactory
import org.publicvalue.multiplatform.oidc.appsupport.PlatformOidcAuthFlow
import org.publicvalue.multiplatform.oidc.sample.Constants
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.screens.ConfigScreen

class HomePresenter(
    val authFlowFactory: AuthFlowFactory,
    val navigator: Navigator
): Presenter<HomeUiState> {
    @Composable
    override fun present(): HomeUiState {
        val scope = rememberCoroutineScope()
        val settingsStore = LocalSettingsStore.current

        val clientSettings by settingsStore.observeClientSettings().collectAsRetainedState()
        val idpSettings by settingsStore.observeIdpSettings().collectAsRetainedState()
        val tokenData by settingsStore.observeTokenData().collectAsRetainedState()

        fun eventSink(event: HomeUiEvent) {
            when(event) {
                HomeUiEvent.Login -> {
                    val clientSettings = clientSettings
                    val idpSettings = idpSettings
                    if (clientSettings != null && idpSettings != null) {
                        scope.launch {
                            val client = OpenIDConnectClient {
                                redirectUri = Constants.redirectUrl.trim()
                                codeChallengeMethod = clientSettings.code_challenge_method
                                this.scope = clientSettings.scope?.trim()
                                this.clientId = clientSettings.client_id?.trim()
                                this.clientSecret = clientSettings.client_secret?.trim()
                                this.endpoints {
                                    authorizationEndpoint = idpSettings.endpointAuthorization?.trim()
                                    tokenEndpoint = idpSettings.endpointToken?.trim()
                                }
                            }

                            val flow = authFlowFactory.createAuthFlow(client)
                            val tokenResponse = flow.getAccessToken()
                            settingsStore.setTokenData(
                                org.publicvalue.multiplatform.oidc.sample.domain.TokenData(
                                    accessToken = tokenResponse.access_token,
                                    refreshToken = tokenResponse.refresh_token,
                                    expiresIn = tokenResponse.expires_in ?: 0,
                                    issuedAt = tokenResponse.received_at
                                )
                            )
                        }
                    }
                }
                HomeUiEvent.Logout -> {

                }
                HomeUiEvent.NavigateToConfig -> {
                    navigator.goTo(ConfigScreen)
                }
                HomeUiEvent.Refresh -> {
                    scope.launch {
                        // TODO
                    }
                }
            }
        }

        return HomeUiState(
            loginEnabled = idpSettings?.isValid() == true && clientSettings?.isValid() == true,
            refreshEnabled = tokenData?.refreshToken != null,
            logoutEnabled = tokenData?.accessToken != null,
            tokenData = tokenData,
            eventSink = ::eventSink
        )
    }
}