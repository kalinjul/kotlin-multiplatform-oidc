# Wasm Project setup
Wasm do not require declaring the redirect scheme, wasm can only use ```https``` with subpaths of your app url.

## AuthFlowFactory
In wasmJs, you can just instantiate the WasmCodeAuthFlowFactory at any time to start authentication.
You may want to set window size parameters in the constructor.

The app will open a new window for the login while the application waits for
the redirect to happen. The redirect is handled by **a new instance of your app** that is opened
inside the login window. Be sure to only call `PlatformCodeAuthFlow.handleRedirect()` in this instance.

This can be achieved using a simple routing mechanism in your wasm application (which also defines
your redirect url):
```kotlin
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
```