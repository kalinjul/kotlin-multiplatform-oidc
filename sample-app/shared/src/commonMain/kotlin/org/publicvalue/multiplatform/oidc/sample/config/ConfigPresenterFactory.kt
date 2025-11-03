package org.publicvalue.multiplatform.oidc.sample.config

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.publicvalue.multiplatform.oidc.sample.screens.ConfigScreen


internal object ConfigPresenterFactory: Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is ConfigScreen -> { ConfigPresenter(navigator) }
            else -> null
        }
    }
}

internal object ConfigUiFactory: Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is ConfigScreen -> {
            ui<ConfigUiState> { state, modifier ->
                Config(state, modifier)
            }
        }
        else -> null
    }
}