import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.Navigator
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.Root
import org.publicvalue.multiplatform.oidc.sample.circuit.UiFactories.Companion.factories
import org.publicvalue.multiplatform.oidc.sample.circuit.UiFactories.Companion.presenterFactories
import org.publicvalue.multiplatform.oidc.settings.SettingsStore

@Composable
internal fun App(
    backstack: SaveableBackStack,
    navigator: Navigator,
    settingsStore: SettingsStore,
    authFlowFactory: CodeAuthFlowFactory
) {
    val circuit = Circuit.Builder()
        .addUiFactories(factories)
        .addPresenterFactories(presenterFactories(authFlowFactory))
        .build()

    Root(
        circuit = circuit,
        backstack = backstack,
        navigator = navigator,
        settingsStore = settingsStore,
    )
}
