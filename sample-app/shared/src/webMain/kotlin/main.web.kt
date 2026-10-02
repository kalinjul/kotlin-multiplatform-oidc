import androidx.compose.runtime.Composable
import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.WebCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.settings.WebMainSettingsStore

@OptIn(ExperimentalOpenIdConnect::class)
@Composable
fun MainView() {
    val settingsStore = WebMainSettingsStore()

    App(
        settingsStore = settingsStore,
        authFlowFactory = WebCodeAuthFlowFactory(
            redirectOrigin = window.location.origin
        )
    )
}
