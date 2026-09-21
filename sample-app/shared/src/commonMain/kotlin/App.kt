import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.Root
import org.publicvalue.multiplatform.oidc.sample.circuit.UiFactories.Companion.presenterFactories
import org.publicvalue.multiplatform.oidc.sample.circuit.UiFactories.Companion.uiFactories
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen
import org.publicvalue.multiplatform.oidc.settings.SettingsStore

@Composable
fun App(
    settingsStore: SettingsStore,
    authFlowFactory: CodeAuthFlowFactory
) {
    val circuit = Circuit.Builder()
    .addUiFactories(uiFactories)
    .addPresenterFactories(presenterFactories(authFlowFactory))
    .build()

    CircuitCompositionLocals(circuit) {
        val backstack = rememberSaveableBackStack(initialScreens = listOf(HomeScreen))

        val navigator = rememberCircuitNavigator(backstack) {}
        Root(
            circuit = circuit,
            backstack = backstack,
            navigator = navigator,
            settingsStore = settingsStore,
        )
    }
}