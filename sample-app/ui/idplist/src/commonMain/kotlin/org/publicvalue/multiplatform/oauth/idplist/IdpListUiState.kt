package org.publicvalue.multiplatform.oauth.idplist

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider

@Immutable
data class IdpListUiState(
    val errorMessage: String?,
    val isLoading: Boolean = false,
    val eventSink: (IdpListUiEvent) -> Unit,
    val idps: List<Identityprovider>
) : CircuitUiState {
}

sealed interface IdpListUiEvent : CircuitUiEvent {
    data class NavigateToIdp(val idp: Identityprovider): IdpListUiEvent
    data object Call: IdpListUiEvent
}
