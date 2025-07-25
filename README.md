# Kotlin Multiplatform OIDC
[![Build](https://img.shields.io/github/actions/workflow/status/kalinjul/kotlin-multiplatform-oidc/main.yml?label=release)]((https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml))
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kalinjul.kotlin.multiplatform/oidc-appsupport)](https://central.sonatype.com/repository/maven-snapshots/io/github/kalinjul/kotlin/multiplatform/oidc-appsupport/)
![Kotlin Version](https://kotlin-version.aws.icerock.dev/kotlin-version?group=io.github.kalinjul.kotlin.multiplatform&name=oidc-appsupport)

Kotlin Multiplatform Library for OpenId Connect / OAuth 2.0.

The library is designed for kotlin multiplatform, Android-only _and_ iOS only Apps.
For iOS only, use the [OpenIdConnectClient Swift Package](https://github.com/kalinjul/OpenIdConnectClient).

This is a lightweight implementation that does not provide any client-side validation of signatures.

Supported platforms:

|         | State        | Implementation                               |
|---------|--------------|----------------------------------------------|
| Android | Stable       | Chrome Custom Tabs                           |
| iOS     | Stable       | ASWebAuthenticationSession                   |
| Desktop | Experimental | Embedded Webserver + Browser                 |
| WasmJS  | Experimental | Popup Window communicating via postMessage() |

Features:
- Only supports [Authorization Code Grant Flow](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
- Support for [discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) via .well-known/openid-configuration.
- Support for [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- Simple JWT parsing (```Jwt.parse()```)
- OkHttp + Ktor integration
- Uses Custom Uri Scheme (my-app://), no support for https redirect uris.

You can find the full Api documentation [here](https://kalinjul.github.io/kotlin-multiplatform-oidc/).

Library dependency versions:

| kmp-oidc version | kotlin version | ktor version |
|------------------|----------------|--------------|
| <=0.11.1         | 1.9.23         | 2.3.7        |
| 0.11.2           | 2.0.20         | 2.3.7        |
| 0.12.+           | 2.0.20         | 3.0.+        |
| 0.13.+           | 2.1.20         | 3.1.+        |
| 0.14.0 - 0.15.+  | 2.1.21         | 3.2.+        |

Note that while the library may work with other kotlin/ktor versions, proceed at your own risk.

# Dependency
Add the dependency to your commonMain sourceSet (KMP) / Android dependencies (android only):
```kotlin
implementation("io.github.kalinjul.kotlin.multiplatform:oidc-appsupport:<version>")
implementation("io.github.kalinjul.kotlin.multiplatform:oidc-ktor:<version>") // optional ktor support
implementation("io.github.kalinjul.kotlin.multiplatform:oidc-okhttp4:<version>") // optional okhttp support (android only)
```

Or, for your libs.versions.toml:
```toml
[versions]
oidc = "<version>"
[libraries]
oidc-appsupport = { module = "io.github.kalinjul.kotlin.multiplatform:oidc-appsupport", version.ref = "oidc" }
oidc-okhttp4 = { module = "io.github.kalinjul.kotlin.multiplatform:oidc-okhttp4", version.ref = "oidc" }
oidc-ktor = { module = "io.github.kalinjul.kotlin.multiplatform:oidc-ktor", version.ref = "oidc" }
```

## Using a snapshot version
If you want try a snapshot version, just add maven("https://central.sonatype.com/repository/maven-snapshots/") to your repositories.
There is currently no way to view available snapshots on sonatype central.

## Compiler options
If you want to run tests, currently you need to pass additional linker flags (adjust the path to your Xcode installation): 
```kotlin
iosSimulatorArm64().compilerOptions {
    freeCompilerArgs.set(listOf("-linker-options", "-L/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/lib/swift/iphonesimulator"))
}
```

# Usage

## Setup
You will need some basic project setup to handle redirect urls and create an instance of the AuthFlowFactory:

[Setup Android](docs/setup-android.md)

[Setup iOS](docs/setup-ios.md)

[Setup Wasm](docs/setup-wasm.md)

## OpenID Configuration
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
    postLogoutRedirectUri = "<postLogoutRedirectUri>"
}
```
If you provide a Discovery URI, you may skip the endpoint configuration and call [discover()](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc/-open-id-connect-client/discover.html) on the client to retrieve the endpoint configuration.

## Authenticate
The Code Auth Flow method is implemented by [CodeAuthFlow](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.flows/-code-auth-flow/index.html). You'll need platform specific variants (see [Setup](#Setup)).
Preferably, those instances should be provided using Dependency Injection.
For more information, have a look at the [KMP sample app](./sample-app).

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

# Ktor support (experimental)
You can use "oidc-ktor" dependency, which provides easy integration for ktor projects:

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

Because of the [way ktor works](https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death), you need to tell the client if the token is invalidated outside of ktor's refresh logic, e.g. on logout:
```kotlin
    ktorHttpClient.clearTokens()
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

```kotlin
val tokens = flow.getAccessToken(configureAuthUrl = {
    // customize url that is passed to browser for authorization requests
    parameters.append("prompt", "login")
}, configureTokenExchange = {
    // customize token exchange http request
    header("additionalHeaderField", "value")
})
```

## End session using GET request and post_logout_redirect_uri
If you have configured a ```postLogoutRedirectUri``` and want to perform a Logout using a Web Flow,
you can use the endSession flow:
```kotlin
val flow = authFlowFactory.createEndSessionFlow(client)
tokens.id_token?.let { flow.endSession(it) }
```
That way, browser cookies should be cleared so the next time a client wants to login, it get's prompted for username and password again.

# JWT Parsing
We provide simple JWT parsing (without any validation):
```kotlin
val jwt = tokens.id_token?.let { Jwt.parse(it) }
println(jwt?.payload?.aud) // print audience
println(jwt?.payload?.iss) // print issuer
println(jwt?.payload?.additionalClaims?.get("email")) // get claim
```

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