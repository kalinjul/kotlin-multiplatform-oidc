import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import org.publicvalue.multiplatform.oidc.appsupport.AndroidAuthFlowFactory
import org.publicvalue.multiplatform.oidc.settings.AndroidSettingsStore
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen

@Composable
fun MainView(
    authFlowFactory: AndroidAuthFlowFactory
) {
    val context = LocalContext.current

    val backstack = rememberSaveableBackStack {
        push(HomeScreen)
    }
    val navigator = rememberCircuitNavigator(backstack) {

    }

    val settingsStore = AndroidSettingsStore(
        context = context.applicationContext
    )

    App(
        backstack = backstack,
        navigator = navigator,
        settingsStore = settingsStore,
        authFlowFactory = authFlowFactory
    )
}
