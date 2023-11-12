package org.publicvalue.multiplatform.oauth.ClientList

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider

@Immutable
data class ClientListUiState(
    val errorMessage: String?,
    val isLoading: Boolean = false,
    val eventSink: (ClientListUiEvent) -> Unit,
    val clients: List<String>
) : CircuitUiState {
}

sealed interface ClientListUiEvent : CircuitUiEvent {
    data class NavigateToClientDetail(val client: String): ClientListUiEvent
}
