import androidx.compose.ui.window.ComposeUIViewController
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import org.publicvalue.multiplatform.oidc.appsupport.IosCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.screens.HomeScreen
import org.publicvalue.multiplatform.oidc.settings.IosSettingsStore
import platform.UIKit.UIViewController

@Suppress("FunctionName")
public fun MainViewController(): UIViewController = ComposeUIViewController {
    val factory = IosCodeAuthFlowFactory()

    val backstack = rememberSaveableBackStack(listOf(HomeScreen))
    val navigator = rememberCircuitNavigator(backstack) {}

    val settingsStore = IosSettingsStore()

    App(
        backstack = backstack,
        navigator = navigator,
        settingsStore = settingsStore,
        authFlowFactory = factory
    )
}
