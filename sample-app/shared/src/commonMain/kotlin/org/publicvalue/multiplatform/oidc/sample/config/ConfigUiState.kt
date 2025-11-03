package org.publicvalue.multiplatform.oidc.sample.config

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oidc.sample.config.ConfigUiEvent
import org.publicvalue.multiplatform.oidc.sample.domain.ClientSettings
import org.publicvalue.multiplatform.oidc.sample.domain.IdpSettings
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.reflect.KProperty1

internal data class ConfigUiState(
    val clientSettings: ClientSettings,
    val idpSettings: IdpSettings,
    val eventSink: (ConfigUiEvent) -> Unit
): CircuitUiState

internal sealed interface ConfigUiEvent: CircuitUiEvent {
    data object NavigateBack: ConfigUiEvent

    data class ChangeDiscoveryUrl(val discoveryUrl: String): ConfigUiEvent
    data class ChangeEndpointToken(val endpointToken: String): ConfigUiEvent
    data class ChangeEndpointAuthorization(val endpointAuthorization: String): ConfigUiEvent
    data class ChangeEndpointEndSession(val endpointEndSession: String): ConfigUiEvent

    data class ChangeClientId(val clientId: String): ConfigUiEvent
    data class ChangeClientSecret(val clientSecret: String): ConfigUiEvent
    data class ChangeScope(val scope: String): ConfigUiEvent
    data class ChangeCodeChallengeMethod(val codeChallengeMethod: CodeChallengeMethod): ConfigUiEvent

    data object Discover: ConfigUiEvent
}