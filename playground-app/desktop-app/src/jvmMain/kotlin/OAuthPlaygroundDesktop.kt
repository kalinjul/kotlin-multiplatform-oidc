import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import org.publicvalue.multiplatform.oauth.inject.create
import org.publicvalue.multiplatform.meldeapp.OauthPlaygroundMainView
import org.publicvalue.multiplatform.oauth.inject.DesktopApplicationComponent
import org.publicvalue.multiplatform.oauth.inject.WindowComponent

fun main() = application {

    val applicationComponent = remember {
        DesktopApplicationComponent.create()
    }

    LaunchedEffect(applicationComponent) {
        applicationComponent.initializers.forEach { it.initialize() }
    }

    val state = rememberWindowState(
        placement = WindowPlacement.Floating,
        size = DpSize(width = 800.dp, height = 800.dp)
    )

    Window(
        title = "OpenIdConnect Playground",
        state = state,
        onCloseRequest = ::exitApplication
    ) {
        val component: WindowComponent = remember(applicationComponent) {
            WindowComponent.create(applicationComponent)
        }

        OauthPlaygroundMainView(
            component
        )
    }
}