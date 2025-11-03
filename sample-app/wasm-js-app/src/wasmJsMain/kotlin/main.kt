import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.PlatformCodeAuthFlow

@OptIn(ExperimentalComposeUiApi::class, ExperimentalOpenIdConnect::class)
internal fun main() {
    @Suppress("DEPRECATION_ERROR")
    CanvasBasedWindow(canvasElementId = "wasm-js-app") {
        val currentPath = window.location.pathname
        when {
            currentPath.isBlank() || currentPath == "/" -> {
                MainView()
            }
            currentPath.startsWith("/redirect") || currentPath.startsWith("/logout") -> {
                LaunchedEffect(Unit) {
                    PlatformCodeAuthFlow.handleRedirect()
                }
            }
        }
    }
}