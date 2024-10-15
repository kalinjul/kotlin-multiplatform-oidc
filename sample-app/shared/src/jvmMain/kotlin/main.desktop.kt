import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen
import org.publicvalue.multiplatform.oidc.settings.JvmSettingsStore

@Composable
fun MainView() {

    val backstack = rememberSaveableBackStack(listOf(HomeScreen))
    val navigator = rememberCircuitNavigator(backstack) {
    }

    val settingsStore = JvmSettingsStore()

    App(
        backstack = backstack,
        navigator = navigator,
        settingsStore = settingsStore,
        authFlowFactory = JvmCodeAuthFlowFactory()
    )
}
