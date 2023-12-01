# Kotlin Multiplatform OIDC
[![Snapshot Build](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml/badge.svg?branch=develop)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml)
[![Release Build](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml)

Library for using OpenID Connect / OAuth 2.0 in Kotlin Multiplatform (iOS+Android), Android and soon Xcode projects.

- Currently, it only supports the [Authorization Code Grant Flow](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
- Support for [discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) via .well-known/openid-configuration.
- Support for [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)

# Usage in Kotlin Multiplatform
Add the dependency to your commonMain sourceSet:
```kotlin
implementation("org.publicvalue.multiplatform.oidc:appsupport:0.1.1")
```

# iOS App usage
## Redirect scheme
For OpenIDConnect/OAuth to work, you have to add the redirect uri scheme to your Info.plist:
In XCode, go to your Project -> Target -> Info -> URL Types.
Add your redirect schema (ex. org.publicvalue.multiplatform.oidc.sample)

## Swift package
Add the swift package from https://github.com/kalinjul/OpenIdConnectClient.
If you're using a swift module, add this:
```swift
    dependencies: [
        .package(name: "OpenIdConnectClient", url: "https://github.com/kalinjul/OpenIdConnectClient", exact: "0.1.1")
    ],
```

## General
Create OpenID config and client:
```swift
import OpenIdConnectClient

let config = clientConfig()

let client = OpenIDConnectClient(
    config: OpenIDConnectClientConfig(
        discoveryUri: "<discovery url>",
        endpoints: Endpoints(
            tokenEndpoint: "<tokenEndpoint>",
            authorizationEndpoint: "<authorizationEndpoint>",
            userInfoEndpoint: nil,
            endSessionEndpoint: "<endSessionEndpoint>"
        ),
        clientId: "<clientId>",
        clientSecret: "<clientSecret>",
        scope: "openid profile",
        codeChallengeMethod: .s256,
        redirectUri: "<redirectUri>"
    )
)
```

Request access token using code auth flow:
```swift 
let flow = OidcCodeAuthFlow(client: client)
do {
    let tokens = try await flow.getAccessToken()
} catch {
    print(error)
}
```

perform refresh or endSession:
```swift
client.refreshToken(refreshToken: tokens.refresh_token)
client.endSession(idToken: tokens.id_token)
```

## Custom headers/url parameters
For most calls (```getAccessToken()```, ```refreshToken()```, ```endSession()```), you may provide
additional configuration for the http call, like headers or parameters using the configure closure parameter:

```swift
try await client.endSession(idToken: idToken) { requestBuilder in
    requestBuilder.headers.append(name: "X-CUSTOM-HEADER", value: "value")
    requestBuilder.url.parameters.append(name: "custom_parameter", value: "value")
}
```

# Android App usage

build.gradle.kts:
```kotlin
    defaultConfig {
        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "<uri scheme>")
        )
    }
```
