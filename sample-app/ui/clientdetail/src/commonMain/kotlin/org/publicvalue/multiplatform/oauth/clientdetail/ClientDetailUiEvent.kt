package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oauth.data.db.Client
import kotlin.reflect.KProperty1

@Immutable
data class ClientDetailUiState(
    val errorMessage: String?,
    val isLoading: Boolean = false,
    val eventSink: (ClientDetailUiEvent) -> Unit,
    val client: Client?,
    val authcodeRequestUrl: String?,
    val authcodeResponseQueryString: String?,
    val authcode: String?,
) : CircuitUiState {
}

sealed interface ClientDetailUiEvent : CircuitUiEvent {
    data object Call: ClientDetailUiEvent
    data object NavigateUp : ClientDetailUiEvent
    data object ResetErrorMessage: ClientDetailUiEvent

    data class ChangeClientProperty<V: Comparable<V>>(val prop: KProperty1<Client, V?>, val value: V):
        ClientDetailUiEvent
    data object Login: ClientDetailUiEvent

}
