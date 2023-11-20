package org.publicvalue.multiplatform.oidc.sample.config

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import org.publicvalue.multiplatform.oidc.sample.data.ClientSettings
import org.publicvalue.multiplatform.oidc.sample.data.IdpSettings
import kotlin.reflect.KProperty1

data class ConfigUiState(
    val clientSettings: ClientSettings,
    val idpSettings: IdpSettings,
    val eventSink: (ConfigUiEvent) -> Unit
): CircuitUiState

sealed interface ConfigUiEvent: CircuitUiEvent {
    data object NavigateBack: ConfigUiEvent
    data class ChangeIdpProperty<V: Comparable<V>>(val prop: KProperty1<IdpSettings, V?>, val value: V): ConfigUiEvent
    data class ChangeClientProperty<V: Comparable<V>>(val prop: KProperty1<ClientSettings, V?>, val value: V): ConfigUiEvent
    data object Discover: ConfigUiEvent
}