import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.appsupport.PlatformCodeAuthFlow

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "wasm-js-app") {
        val currentPath = window.location.pathname
        when {
            currentPath.isBlank() || currentPath == "/" -> {
                MainView()
            }
            currentPath.startsWith("/redirect") -> {
                LaunchedEffect(Unit) {
                    PlatformCodeAuthFlow.handleRedirect()
                }
            }
        }
    }
}