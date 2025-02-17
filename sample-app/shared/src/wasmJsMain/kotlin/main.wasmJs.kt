import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.appsupport.WasmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen
import org.publicvalue.multiplatform.oidc.settings.WasmJsSettingsStore

@Composable
fun MainView() {
    val backstack = rememberSaveableBackStack(listOf(HomeScreen))

    val navigator = rememberCircuitNavigator(backstack) {}

    val settingsStore = WasmJsSettingsStore()

    App(
        backstack = backstack,
        navigator = navigator,
        settingsStore = settingsStore,
        authFlowFactory = WasmCodeAuthFlowFactory(
            redirectOrigin = window.location.origin
        )
    )
}
