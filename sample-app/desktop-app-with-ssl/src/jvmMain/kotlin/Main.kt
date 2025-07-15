import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.SslSampleApp

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating,
        width = 1200.dp,
        height = 800.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "OIDC SSL Sample Application"
    ) {
        SslSampleApp()
    }
}