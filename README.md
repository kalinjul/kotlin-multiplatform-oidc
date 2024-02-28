# Kotlin Multiplatform OIDC
[![Snapshot Build](https://img.shields.io/github/actions/workflow/status/kalinjul/kotlin-multiplatform-oidc/develop.yml?label=snapshot)]((https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml))
[![Release Build](https://img.shields.io/github/actions/workflow/status/kalinjul/kotlin-multiplatform-oidc/main.yml?label=release)]((https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml))
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kalinjul.kotlin.multiplatform/oidc-appsupport)](https://repo1.maven.org/maven2/io/github/kalinjul/kotlin/multiplatform/oidc-appsupport/)
![Kotlin Version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=io.github.kalinjul.kotlin.multiplatform&name=oidc-appsupport)


Library for using OpenId Connect / OAuth 2.0 in Kotlin Multiplatform (iOS+Android), Android and Xcode projects.
This project aims to be a lightweight implementation without sophisticated validation on client side.
Simple Desktop support is included via an embedded Webserver that listens for redirects.

- Currently, it only supports the [Authorization Code Grant Flow](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
- Support for [discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) via .well-known/openid-configuration.
- Support for [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- Uses ```ASWebAuthenticationSession``` (iOS), Chrome Custom Tabs (Android), Embedded Webserver + Browser (Desktop)
- Simple JWT parsing
- OkHttp + Ktor support

The library is designed for kotlin multiplatform, Android-only _and_ iOS only Apps.
For iOS only, use the [OpenIdConnectClient Swift Package](https://github.com/kalinjul/OpenIdConnectClient).

You can find the full Api documentation [here](https://kalinjul.github.io/kotlin-multiplatform-oidc/).

# Dependency
Add the dependency to your commonMain sourceSet (KMP) / Android dependencies (android only):
```kotlin
implementation("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport:0.8.3")
implementation("io.github.kalinjul.kotlin.multiplatform:oidc-okhttp4:0.8.3") // optional, android only
implementation("io.github.kalinjul.kotlin.multiplatform:oidc-ktor:0.8.3") // optional ktor support
```

Or, for your libs.versions.toml:
```toml
[versions]
oidc = "0.8.3"
[libraries]
oidc-appsupport = { module = "io.github.kalinjul.kotlin.multiplatform:oidc-appsupport", version.ref = "oidc" }
oidc-okhttp4 = { module = "io.github.kalinjul.kotlin.multiplatform:oidc-okhttp4", version.ref = "oidc" }
oidc-ktor = { module = "io.github.kalinjul.kotlin.multiplatform:oidc-ktor", version.ref = "oidc" }
```

## Compiler options
If you want to run tests, currently (as of kotlin 1.9.22), you need to pass additional linker flags (adjust the path to your Xcode installation): 
```kotlin
iosSimulatorArm64().compilations.all {
    kotlinOptions {
        freeCompilerArgs = listOf("-linker-options", "-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphonesimulator")
    }
}
```

# Usage
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
iOS does not require declaring the redirect scheme.

## OpenID Configuration (common code)
Create an [OpenIdConnectClient](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc/-open-id-connect-client/index.html):
```kotlin
val client = OpenIdConnectClient(discoveryUri = "<discovery url>") {
    endpoints {
        tokenEndpoint = "<tokenEndpoint>"
        authorizationEndpoint = "<authorizationEndpoint>"
        userInfoEndpoint = null
        endSessionEndpoint = "<endSessionEndpoint>"
    }

    clientId = "<clientId>"
    clientSecret = "<clientSecret>"
    scope = "openid profile"
    codeChallengeMethod = CodeChallengeMethod.S256
    redirectUri = "<redirectUri>"
}
```
If you provide a Discovery URI, you may skip the endpoint configuration and call [discover()](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc/-open-id-connect-client/discover.html) on the client to retrieve the endpoint configuration.

## Create a Code Auth Flow instance (platform specific)
The Code Auth Flow method is implemented by [CodeAuthFlow](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.flows/-code-auth-flow/index.html). You'll need platform specific variants, so we'll use a factory to get an instance.

For Android, create an instance of [AndroidCodeAuthFlowFactory](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.appsupport/-android-code-auth-flow-factory/index.html) in your Activity's onCreate():

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = AndroidCodeAuthFlowFactory()
        factory.registerActivity(this)
    }
}
```
> [!IMPORTANT]  
> The Factory MUST be instanciated (or more precise: registerActivity() must be called) in onCreate()
> or earlier, as it will attach to the ComponentActivity's lifecycle.
> If you don't use ComponentActivity, you need to implement your own Factory.
> 
> If you want to use Dependency Injection and don't have access to your activity, you can register 
> the factory without an activity, inject it into your onCreate() Method and call registerActivity() 
> from there. 

For the iOS part, you can use [IosCodeAuthFlowFactory](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.appsupport/-ios-code-auth-flow-factory/index.html). 
Both factories implement a common interface and can be provided using Dependency Injection.

For more information, have a look at the [KMP sample app](./sample-app).

## Authenticate (common code)
Request tokens using code auth flow (this will open the browser for login):
```kotlin 
val flow = authFlowFactory.createAuthFlow(client)
val tokens = flow.getAccessToken()
```

Perform refresh or endSession:
```kotlin
tokens.refresh_token?.let { client.refreshToken(refreshToken = it) }
tokens.id_token?.let { client.endSession(idToken = it) }
```

## Custom headers/url parameters
For most calls (```getAccessToken()```, ```refreshToken()```, ```endSession()```), you may provide
additional configuration for the http call, like headers or parameters using the configure closure parameter:

```kotlin
client.endSession(idToken = idToken) {
    headers.append("X-CUSTOM-HEADER", "value")
    url.parameters.append("custom_parameter", "value")
}
```

# JWT Parsing
We provide simple JWT parsing (without any validation):
```kotlin
val jwt = tokens.id_token?.let { Jwt.parse(it) }
println(jwt?.payload?.aud) // print audience
println(jwt?.payload?.iss) // print issuer
println(jwt?.payload?.additionalClaims?.get("email")) // get claim
```

# Token Store (experimental)
Since persisting tokens is a common task in OpenID Connect Authentication, we provide a 
[TokenStore](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.tokenstore/-token-store/index.html) that uses a [Multiplatform Settings Library](https://github.com/russhwolf/multiplatform-settings)
to persist tokens in Keystore (iOS) / Encrypted Preferences (Android).
If you use the TokenStore, you may also make use of [TokenRefreshHandler](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.tokenstore/-token-refresh-handler/index.html) for synchronized token
refreshes.
```kotlin
tokenstore.saveTokens(tokens)
val accessToken = tokenstore.getAccessToken()

val refreshHandler = TokenRefreshHandler(tokenStore = tokenstore)
refreshHandler.refreshAndSaveToken(client, oldAccessToken = token) // thread-safe refresh and save new tokens to store
```
Android implementation is [AndroidEncryptedPreferencesSettingsStore](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.tokenstore/-android-encrypted-preferences-settings-store/index.html), for iOS use [IosKeychainTokenStore](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.tokenstore/-ios-keychain-token-store/index.html).

# OkHttp support (Android only) (experimental)
```kotlin
val authenticator = OpenIdConnectAuthenticator {
    getAccessToken { tokenStore.getAccessToken() }
    refreshTokens { oldAccessToken -> refreshHandler.refreshAndSaveToken(client, oldAccessToken) }
    onRefreshFailed {
        // provided by app: user has to authenticate again
    }
    buildRequest {
        header("AdditionalHeader", "value") // add custom header to all requests
    }
}

val okHttpClient = OkHttpClient.Builder()
    .authenticator(authenticator)
    .build()
```

# Ktor support (experimental)
```kotlin
    HttpClient(engine) {
        install(Auth) {
            oidcBearer(
                tokenStore = tokenStore,
                refreshHandler = refreshHandler,
                client = client,
            )
        }
    }
}
```

Because of the [way ktor works](https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death), you need to tell the client if the token is invalidated outside of 
ktor's refresh logic, e.g. on logout:
```kotlin
    ktorHttpClient.clearTokens()
```