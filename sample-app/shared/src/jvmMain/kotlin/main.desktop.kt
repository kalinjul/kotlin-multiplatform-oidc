import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.JvmCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.settings.JvmSettingsStore

@OptIn(ExperimentalOpenIdConnect::class)
@Composable
fun MainView() {
    val settingsStore = JvmSettingsStore()

    App(
        settingsStore = settingsStore,
        authFlowFactory = remember { JvmCodeAuthFlowFactory() }
    )
}
