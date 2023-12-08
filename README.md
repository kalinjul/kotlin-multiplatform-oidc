# Kotlin Multiplatform OIDC
[![Snapshot Build](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml/badge.svg?branch=develop)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml)
[![Release Build](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kalinjul.kotlin.multiplatform/oidc-appsupport)](https://repo1.maven.org/maven2/io/github/kalinjul/kotlin/multiplatform/oidc-appsupport/)
[![Docs](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/static-docs.yml/badge.svg)](https://kalinjul.github.io/kotlin-multiplatform-oidc/)

Library for using OpenId Connect / OAuth 2.0 in Kotlin Multiplatform (iOS+Android), Android and Xcode projects.
This project aims to be a lightweight implementation without sophisticated validation on client side.

- Currently, it only supports the [Authorization Code Grant Flow](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
- Support for [discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) via .well-known/openid-configuration.
- Support for [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- Simple JWT parsing

You can find the full Api documentation [here](https://kalinjul.github.io/kotlin-multiplatform-oidc/).

# Add dependency for Kotlin Multiplatform or Android
Add the dependency to your commonMain sourceSet (KMP) / Android dependencies (android only):
```kotlin
implementation("org.publicvalue.multiplatform.oidc:appsupport:0.4.1")
implementation("org.publicvalue.multiplatform.oidc:okhttp4:0.4.1") // optional, android only
```

# iOS App usage
See [OpenIdConnectClient Swift Package](https://github.com/kalinjul/OpenIdConnectClient)

# Android App usage
## Redirect scheme
For OpenIDConnect/OAuth to work, you have to provide the redirect uri in your build.gradle:

build.gradle.kts:
```kotlin
    defaultConfig {
        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "<uri scheme>")
        )
    }
```

## General
Create OpenID config and client:
```kotlin
    val config = OpenIdConnectClient(discoveryUri = "<discovery url>") {
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
If you provide a Discovery URI, you may skip the endpoint configuration.

Create an instance of AuthFlowFactory in your Activity's onCreate():
```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = AndroidCodeAuthFlowFactory(this)
    }
}
```
For multiplatform projects, there is also IosAuthFlowFactory. 
For more information, have a look in to the [KMP sample app](./sample-app).

Request tokens using code auth flow:
```kotlin 
val flow = authFlowFactory.createAuthFlow(client)
val tokens = flow.getAccessToken()
```

perform refresh or endSession:
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
We provide simple JWT parsing:
```kotlin
val jwt = tokens.id_token?.let { Jwt.parse(it) }
println(jwt?.payload?.aud) // print audience
println(jwt?.payload?.iss) // print issuer
println(jwt?.payload?.additionalClaims?.get("email")) // get claim
```

# Token Store (experimental)
Since persisting tokens is a common task in OpenID Connect Authentication, we provide a 
```TokenStore``` that uses a [Multiplatform Settings Library](https://github.com/russhwolf/multiplatform-settings)
to persist tokens in Keystore (iOS) / Encrypted Preferences (Android).
If you use the TokenStore, you may also make use of ```TokenRefreshHandler``` for synchronized token
refreshes.
```kotlin
tokenstore.saveTokens(tokens)
val accessToken = tokenstore.getAccessToken()

val refreshHandler = TokenRefreshHandler(tokenStore = tokenstore)
refreshHandler.safeRefreshToken(client, oldAccessToken = accessToken) // thread-safe refresh and save new tokens to store
```
Android implementation is ```AndroidEncryptedPreferencesSettingsStore```, for iOS use ```IosKeychainTokenStore```.

# OkHttp support (experimental)
```kotlin
val authenticator = OpenIdConnectAuthenticator {
    getAccessToken { tokenStore.getAccessToken() }
    refreshTokens { oldAccessToken -> refreshHandler.safeRefreshToken(client, oldAccessToken) }
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