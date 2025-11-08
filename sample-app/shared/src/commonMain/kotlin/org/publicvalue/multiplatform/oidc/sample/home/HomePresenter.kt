package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.retained.rememberRetained
import com.slack.circuit.runtime.Navigator
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.isSuccess
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

internal class HomePresenter(
    val authFlowFactory: CodeAuthFlowFactory,
    val navigator: Navigator
) : ErrorPresenter<HomeUiState> {

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
                    codeChallengeMethod = clientSettings.codeChallengeMethod
                    this.scope = clientSettings.scope?.trim()
                    this.clientId = clientSettings.clientId?.trim()
                    this.clientSecret = clientSettings.clientSecret?.trim()
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
            val jwt = newTokens.idToken?.let { Jwt.parse(it) }
            println("parsed jwt: $jwt")
            subject = jwt?.payload?.sub
            settingsStore.setTokenData(
                org.publicvalue.multiplatform.oidc.sample.domain.TokenData(
                    accessToken = newTokens.accessToken,
                    refreshToken = newTokens.refreshToken,
                    idToken = newTokens.idToken,
                    expiresIn = newTokens.expiresIn ?: 0,
                    issuedAt = newTokens.receivedAt
                )
            )
        }

        fun eventSink(event: HomeUiEvent) {
            when (event) {
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
                                        val result = if (isGoogle) {
                                            val endpoint = "https://accounts.google.com/o/oauth2/revoke"
                                            val url = URLBuilder(endpoint)
                                            val response = DefaultHttpClient.submitForm {
                                                url(url.build())
                                                parameter("token", it.accessToken)
                                            }
                                            response.status
                                        } else {
                                            if (event.useWebFlow) {
                                                val flow = authFlowFactory.createEndSessionFlow(client)
                                                val result = flow.endSession(it.idToken ?: "")
                                                if (result.isFailure) {
                                                    setErrorMessage(
                                                        result.exceptionOrNull()?.message ?: "Unknown error"
                                                    )
                                                }
                                                if (result.isSuccess) HttpStatusCode.OK else null
                                            } else {
                                                client.endSession(idToken = it.idToken ?: "")
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
                                    val newTokens = client.refreshToken(refreshToken = it.refreshToken ?: "")
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
