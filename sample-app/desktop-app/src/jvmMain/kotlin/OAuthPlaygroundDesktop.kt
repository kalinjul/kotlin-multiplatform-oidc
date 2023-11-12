import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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

    Window(onCloseRequest = ::exitApplication) {
        val component: WindowComponent = remember(applicationComponent) {
            WindowComponent.create(applicationComponent)
        }

        OauthPlaygroundMainView(
            component
        )
    }
}