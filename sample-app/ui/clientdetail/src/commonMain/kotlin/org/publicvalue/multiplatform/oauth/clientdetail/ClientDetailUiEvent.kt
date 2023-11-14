package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oauth.data.db.Client

@Immutable
data class ClientDetailUiState(
    val errorMessage: String?,
    val isLoading: Boolean = false,
    val eventSink: (ClientDetailUiEvent) -> Unit,
    val client: Client?
) : CircuitUiState {
}

sealed interface ClientDetailUiEvent : CircuitUiEvent {
    data object Call: ClientDetailUiEvent
    data object NavigateUp : ClientDetailUiEvent
}
