package org.publicvalue.multiplatform.oidc.sample.home

import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen

internal class HomePresenterFactory(val authFlowFactory: CodeAuthFlowFactory) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? {
        return when (screen) {
            is HomeScreen -> { HomePresenter(authFlowFactory, navigator) }
            else -> null
        }
    }
}

internal object HomeUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is HomeScreen -> {
            ui<HomeUiState> { state, modifier ->
                Home(state, modifier)
            }
        }
        else -> null
    }
}
