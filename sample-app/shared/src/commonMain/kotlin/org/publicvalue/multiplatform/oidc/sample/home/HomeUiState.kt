package org.publicvalue.multiplatform.oidc.sample.home

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oidc.sample.domain.TokenData

internal data class HomeUiState(
    val loginEnabled: Boolean,
    val refreshEnabled: Boolean,
    val logoutEnabled: Boolean,
    val tokenData: TokenData?,
    val subject: String?,
    val eventSink: (HomeUiEvent) -> Unit,
    val errorMessage: String?
) : CircuitUiState

internal sealed interface HomeUiEvent : CircuitUiEvent {
    data object NavigateToConfig : HomeUiEvent
    data object Login : HomeUiEvent
    data class Logout(val useWebFlow: Boolean) : HomeUiEvent
    data object Refresh : HomeUiEvent
}
