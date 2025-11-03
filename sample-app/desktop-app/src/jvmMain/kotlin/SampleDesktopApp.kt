import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

internal fun main() = application {

    val state = rememberWindowState(
        placement = WindowPlacement.Floating,
        size = DpSize(width = 800.dp, height = 800.dp)
    )

    Window(
        title = "Oauth Sample App",
        state = state,
        onCloseRequest = ::exitApplication
    ) {

        MainView()
    }
}