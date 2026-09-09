import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.settings.AndroidSettingsStore

@Composable
fun MainView(
    authFlowFactory: AndroidCodeAuthFlowFactory
) {
    val context = LocalContext.current

    val settingsStore = AndroidSettingsStore(
        context = context.applicationContext
    )

    App(
        settingsStore = settingsStore,
        authFlowFactory = authFlowFactory
    )
}
