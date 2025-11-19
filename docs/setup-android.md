# Android Project setup

## Redirect scheme
For OpenIDConnect/OAuth to work, you have to provide the redirect uri in your Android App's build.gradle:

build.gradle.kts:
```kotlin
android {
    defaultConfig {
        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "<uri scheme>")
        )
    }
}
```

## AuthFlowFactory
You should have a single global instance of [AndroidCodeAuthFlowFactory](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.appsupport/-android-code-auth-flow-factory/index.html), preferably
using Dependency Injection.
You will than need to register your activity in your Activity's onCreate():

```kotlin
class MainActivity : ComponentActivity() {
    // There should only be one instance of this factory.
    // The flow should also be created and started from an
    // Application or ViewModel scope, so it persists Activity.onDestroy() e.g. on low memory
    // and is still able to process redirect results during login.
    val codeAuthFlowFactory = AndroidCodeAuthFlowFactory(useWebView = false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeAuthFlowFactory.registerActivity(this)
    }
}
```
> [!IMPORTANT]  
> You MUST register your activity using registerActivity() in onCreate() or earlier, as the factory
> will attach to the ComponentActivity's lifecycle.
> If you don't use ComponentActivity, you'll need to implement your own Factory.

## Login/Logout continuation
As the actual authentication is performed in a Web Browser, it is possible, especially on low-end devices, that your application is terminated while in background.
This behaviour can be forced by using ```adb shell am kill <app id>```.
To continue the login flow on application restart, call ```authFlow.continueLogin()``` on startup:
```
if (authFlow.canContinueLogin()) {
    val tokens = authFlow.continueLogin(configureTokenExchange = null)
    // save tokens
}
```

To continue a logout flow on application restart:
```
if (endSessionFlow.canContinueLogout()) {
    endSessionFlow.continueLogout()
    // clear tokens
}
```

## Verified App-Links as Redirect Url
If you want to use [https redirect links instead of custom schemes](https://github.com/kalinjul/kotlin-multiplatform-oidc/issues/46), you can do so by replacing the original intent filter in your AndroidManifest.xml:

```
<activity
    android:name="org.publicvalue.multiplatform.oidc.appsupport.HandleRedirectActivity"
    tools:ignore="IntentFilterExportedReceiver"> <!-- Android Studio erroneously shows a warning otherwise -->
    <intent-filter tools:node="removeAll" /> <!-- Optional: remove the original intent filter defined by oidc-appsupport-android - If you do so, you can then also remove the oidcRedirectScheme definition in Gradle  -->
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />

        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />

        <data android:scheme="https" android:host="example.com" />
    </intent-filter>
</activity>
```