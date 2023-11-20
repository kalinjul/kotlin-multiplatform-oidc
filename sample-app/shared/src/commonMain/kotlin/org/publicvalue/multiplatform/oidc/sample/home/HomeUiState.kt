package org.publicvalue.multiplatform.oidc.sample.home

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oidc.sample.domain.TokenData

data class HomeUiState(
    val loginEnabled: Boolean,
    val refreshEnabled: Boolean,
    val logoutEnabled: Boolean,
    val tokenData: TokenData,
    val eventSink: (HomeUiEvent) -> Unit
): CircuitUiState

sealed interface HomeUiEvent: CircuitUiEvent {
    data object NavigateToConfig: HomeUiEvent
    data object Login: HomeUiEvent
    data object Logout: HomeUiEvent
    data object Refresh: HomeUiEvent
}