package org.publicvalue.multiplatform.oauth.clientlist2

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.db.Identityprovider
import kotlin.reflect.KProperty1

@Immutable
data class ClientListUiState(
    val errorMessage: String?,
    val isLoading: Boolean = false,
    val eventSink: (ClientListUiEvent) -> Unit,
    val clients: List<Client>,
    val idp: Identityprovider?
) : CircuitUiState {
}

sealed interface ClientListUiEvent : CircuitUiEvent {
    data class NavigateToClientDetail(val client: Client): ClientListUiEvent
    data class RemoveClient(val client: Client): ClientListUiEvent

    /**
     * kotlin generic infers intersection types...
     * https://stackoverflow.com/questions/72668828/kotlin-custom-scope-function-return-type-not-behaving-as-expected/72668941#72668941
     */
    data class ChangeIdpProperty<V: Comparable<V>>(val prop: KProperty1<Identityprovider, V?>, val value: V):
        ClientListUiEvent
//    data class ChangeIdpProperty<T: KProperty1<Identityprovider, U?>, U>(val prop: T, val value: U):
//        ClientListUiEvent

    data object AddClient: ClientListUiEvent
    data object NavigateUp: ClientListUiEvent
    data object Discover: ClientListUiEvent
    data object ResetErrorMessage : ClientListUiEvent
}