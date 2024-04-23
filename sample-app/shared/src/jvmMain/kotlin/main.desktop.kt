import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import org.publicvalue.multiplatform.oidc.appsupport.JvmAuthFlowFactory
import org.publicvalue.multiplatform.oidc.settings.JvmSettingsStore
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen

@Composable
fun MainView() {

    val backstack = rememberSaveableBackStack {
        push(HomeScreen)
    }
    val navigator = rememberCircuitNavigator(backstack) {

    }

    val settingsStore = JvmSettingsStore()

    App(
        backstack = backstack,
        navigator = navigator,
        settingsStore = settingsStore,
        authFlowFactory = JvmAuthFlowFactory()
    )
}
