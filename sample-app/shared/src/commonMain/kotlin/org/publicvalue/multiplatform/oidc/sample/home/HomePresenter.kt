package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.AuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.Constants
import org.publicvalue.multiplatform.oidc.sample.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oidc.sample.circuit.catchErrorMessage
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.screens.ConfigScreen
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.types.Jwt

class HomePresenter(
    val authFlowFactory: AuthFlowFactory,
    val navigator: Navigator
): ErrorPresenter<HomeUiState> {

    override var errorMessage = MutableStateFlow<String?>(null)

    @Composable
    override fun present(): HomeUiState {
        val scope = rememberCoroutineScope()
        val settingsStore = LocalSettingsStore.current

        val clientSettings by settingsStore.observeClientSettings().collectAsRetainedState()
        val idpSettings by settingsStore.observeIdpSettings().collectAsRetainedState()
        val tokenData by settingsStore.observeTokenData().collectAsRetainedState()

        var tokenResponse by rememberRetained { mutableStateOf<AccessTokenResponse?>(null) }
        var subject by rememberRetained { mutableStateOf<String?>(null) }

        val errorMessage by this.errorMessage.collectAsRetainedState()

        fun createClient(): OpenIDConnectClient? {
            val clientSettings = clientSettings
            val idpSettings = idpSettings
            return if (clientSettings != null && idpSettings != null) {
                OpenIDConnectClient(idpSettings.discoveryUrl) {
                    redirectUri = Constants.redirectUrl.trim()
                    codeChallengeMethod = clientSettings.code_challenge_method
                    this.scope = clientSettings.scope?.trim()
                    this.clientId = clientSettings.client_id?.trim()
                    this.clientSecret = clientSettings.client_secret?.trim()
                    this.endpoints {
                        authorizationEndpoint = idpSettings.endpointAuthorization?.trim()
                        tokenEndpoint = idpSettings.endpointToken?.trim()
                        endSessionEndpoint = idpSettings.endpointEndSession?.trim()
                    }
                }
            } else {
                null
            }
        }

        suspend fun updateTokenResponse(newTokens: AccessTokenResponse) {
            tokenResponse = newTokens
            val jwt = newTokens.id_token?.let { Jwt.parse(it) }
            subject = jwt?.payload?.sub
            settingsStore.setTokenData(
                org.publicvalue.multiplatform.oidc.sample.domain.TokenData(
                    accessToken = newTokens.access_token,
                    refreshToken = newTokens.refresh_token,
                    idToken = newTokens.id_token,
                    expiresIn = newTokens.expires_in ?: 0,
                    issuedAt = newTokens.received_at
                )
            )
        }

        fun eventSink(event: HomeUiEvent) {
            when(event) {
                HomeUiEvent.Login -> {
                    resetErrorMessage()
                    val client = createClient()

                    if (client != null) {
                        scope.launch {
                            catchErrorMessage {
                                val newTokens = authFlowFactory.createAuthFlow(client).getAccessToken()
                                updateTokenResponse(newTokens)
                            }
                        }
                    }
                }
                HomeUiEvent.Logout -> {
                    resetErrorMessage()
                    val client = createClient()
                    if (client != null) {
                        tokenResponse?.let {
                            scope.launch {
                                if (!client.config.endpoints.endSessionEndpoint.isNullOrEmpty()) {
                                    catchErrorMessage {
                                        val result = client.endSession(idToken = it.id_token ?: "")
                                        if (result.isSuccess() || result == HttpStatusCode.Found) {
                                            tokenResponse = null
                                            settingsStore.clearTokenData()
                                        }
                                    }
                                } else {
                                    setErrorMessage("No endSessionEndpoint set")
                                }
                            }
                        }
                    }
                }
                HomeUiEvent.NavigateToConfig -> {
                    navigator.goTo(ConfigScreen)
                }
                HomeUiEvent.Refresh -> {
                    resetErrorMessage()
                    val client = createClient()
                    if (client != null) {
                        scope.launch {
                            catchErrorMessage {
                                tokenResponse?.let {
                                    val newTokens = client.refreshToken(refreshToken = it.refresh_token ?: "")
                                    updateTokenResponse(newTokens)
                                }
                            }
                        }
                    }
                }
            }
        }

        return HomeUiState(
            loginEnabled = idpSettings?.isValid() == true && clientSettings?.isValid() == true,
            refreshEnabled = tokenData?.refreshToken != null,
            logoutEnabled = tokenData?.accessToken != null,
            tokenData = tokenData,
            subject = subject,
            eventSink = ::eventSink,
            errorMessage = errorMessage
        )
    }
}