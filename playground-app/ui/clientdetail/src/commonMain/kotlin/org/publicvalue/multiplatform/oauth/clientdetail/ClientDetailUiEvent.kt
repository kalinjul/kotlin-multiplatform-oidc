package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse
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
    val tokenRequestParameters: Parameters?,
    val tokenResponse: AccessTokenResponse?,
    val tokenResponseStatusCode: HttpStatusCode?,
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
