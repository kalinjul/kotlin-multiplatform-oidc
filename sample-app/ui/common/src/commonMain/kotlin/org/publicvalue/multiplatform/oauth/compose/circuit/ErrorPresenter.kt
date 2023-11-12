package org.publicvalue.multiplatform.oauth.compose.circuit

import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.presenter.Presenter
import kotlinx.coroutines.flow.MutableStateFlow

interface ErrorPresenter<UiState : CircuitUiState>: Presenter<UiState> {
    var errorMessage: MutableStateFlow<String?>

    fun resetErrorMessage() {
        this.errorMessage.value = null
    }
}

//fun <Result> CollectStatusContext<Result>.useError(presenter: ErrorPresenter<*>) {
//    onStart { presenter.errorMessage.value = null }
//    onFailure { presenter.errorMessage.value = it.message }
//}