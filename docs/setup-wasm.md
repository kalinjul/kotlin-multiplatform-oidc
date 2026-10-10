# Web (Wasm/JS) Project setup
Web targets (wasmJs and js) do not require declaring the redirect scheme. They can only use redirect urls that are subpaths of your app url (same origin).

## AuthFlowFactory
On web targets, you can just instantiate the `WebCodeAuthFlowFactory` at any time to start authentication.
You may want to set window size parameters (`windowFeatures`) in the constructor.

The app will open a new window for the login while the application waits for
the redirect to happen. The redirect is handled by **a new instance of your app** that is opened
inside the login window. Be sure to only call `PlatformCodeAuthFlow.handleRedirect()` in this instance.
The same applies to your `postLogoutRedirectUri` if you use the end session flow.

This can be achieved using a simple routing mechanism in your wasm application (which also defines
your redirect url):
```kotlin
fun main() {
    ComposeViewport(document.body!!) {
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
```