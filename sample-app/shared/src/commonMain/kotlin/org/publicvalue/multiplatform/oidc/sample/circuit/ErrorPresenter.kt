package org.publicvalue.multiplatform.oidc.sample.circuit

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.MutableStateFlow

interface ErrorPresenter<UiState : CircuitUiState>: Presenter<UiState> {
    var errorMessage: MutableStateFlow<String?>

    fun resetErrorMessage() {
        this.errorMessage.value = null
    }

    fun setErrorMessage(value: String) {
        this.errorMessage.value = value
    }
}

suspend fun <T: ErrorPresenter<UiState>, UiState> T.catchErrorMessage(block: suspend T.() -> Unit) {
    try {
        block()
    } catch (t: Throwable) {
        t.printStackTrace()
        errorMessage.value = t.message
    }
}
