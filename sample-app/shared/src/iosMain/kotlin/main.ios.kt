import androidx.compose.ui.window.ComposeUIViewController
import org.publicvalue.multiplatform.oidc.appsupport.IosCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.settings.IosSettingsStore

fun MainViewController() = ComposeUIViewController {
    val factory = IosCodeAuthFlowFactory()

    val settingsStore = IosSettingsStore()

    App(
        settingsStore = settingsStore,
        authFlowFactory = factory
    )
}