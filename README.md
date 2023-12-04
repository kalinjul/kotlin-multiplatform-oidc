# Kotlin Multiplatform OIDC
[![Snapshot Build](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml/badge.svg?branch=develop)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/develop.yml)
[![Release Build](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml/badge.svg?branch=main)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/main.yml)

Library for using OpenId Connect / OAuth 2.0 in Kotlin Multiplatform (iOS+Android), Android and Xcode projects.

- Currently, it only supports the [Authorization Code Grant Flow](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
- Support for [discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) via .well-known/openid-configuration.
- Support for [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- Simple JWT parsing

# Usage in Kotlin Multiplatform
Add the dependency to your commonMain sourceSet:
```kotlin
implementation("org.publicvalue.multiplatform.oidc:appsupport:0.1.1")
```

# iOS App usage
See [OpenIdConnectClient Swift Package](https://github.com/kalinjul/OpenIdConnectClient)

# Android App usage

build.gradle.kts:
```kotlin
    defaultConfig {
        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "<uri scheme>")
        )
    }
```

# JWT Parsing
We provide simple JWT parsing:
Kotlin:
```kotlin
val jwt = tokens.id_token?.let { Jwt.parse(it) }
println(jwt?.payload?.aud) // print audience
println(jwt?.payload?.iss) // print issuer
println(jwt?.payload?.additionalClaims?.get("email")) // get claim
```
Swift:
```swift
TODO
```