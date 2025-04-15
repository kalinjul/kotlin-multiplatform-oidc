package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.runtime.*
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.publicvalue.multiplatform.oidc.DefaultOpenIdConnectClient.Companion.DefaultHttpClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.PlatformConstants
import org.publicvalue.multiplatform.oidc.sample.circuit.ErrorPresenter
import org.publicvalue.multiplatform.oidc.sample.circuit.catchErrorMessage
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.screens.ConfigScreen
import org.publicvalue.multiplatform.oidc.types.Jwt
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

class HomePresenter(
    val authFlowFactory: CodeAuthFlowFactory,
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

        fun createClient(): OpenIdConnectClient? {
            val clientSettings = clientSettings
            val idpSettings = idpSettings
            return if (clientSettings != null && idpSettings != null) {
                OpenIdConnectClient(idpSettings.discoveryUrl) {
                    redirectUri = PlatformConstants.redirectUrl.trim()
                    postLogoutRedirectUri = PlatformConstants.postLogoutRedirectUrl.trim()
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
            println("parsed jwt: $jwt")
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
                                val newTokens = authFlowFactory.createAuthFlow(client).getAccessToken(
                                    configureAuthUrl = {
//                                        parameters.append("prompt", "login")
                                    }
                                )
                                updateTokenResponse(newTokens)
                            }
                        }
                    }
                }
                is HomeUiEvent.Logout -> {
                    resetErrorMessage()
                    val client = createClient()
                    if (client != null) {
                        tokenResponse?.let {
                            scope.launch {
                                val isGoogle = client.config.discoveryUri.toString().contains("accounts.google.com")
                                if (!client.config.endpoints?.endSessionEndpoint.isNullOrEmpty() || isGoogle) {
                                    catchErrorMessage {
                                        val result = if(isGoogle) {
                                            val endpoint = "https://accounts.google.com/o/oauth2/revoke"
                                            val url = URLBuilder(endpoint)
                                            val response = DefaultHttpClient.submitForm {
                                                url(url.build())
                                                parameter("token", it.access_token)
                                            }
                                            response.status
                                        } else {
                                            if (event.useEndSessionFlow) {
                                                val flow = authFlowFactory.createEndSessionFlow(client)
                                                val result = flow.endSession(it.id_token ?: "")
                                                if (result.isFailure) {
                                                    setErrorMessage(result.exceptionOrNull()?.message ?: "Unknown error")
                                                }
                                                if (result.isSuccess) HttpStatusCode.OK else null
                                            } else {
                                                client.endSession(idToken = it.id_token ?: "")
                                            }
                                        }
                                        if (result?.isSuccess() == true || result == HttpStatusCode.Found) {
                                            tokenResponse = null
                                            subject = null
                                            settingsStore.clearTokenData()
                                        } else {
                                            setErrorMessage("Logout received $result")
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