# Kotlin Multiplatform OIDC
[![Build Snapshot](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/build.yml/badge.svg)](https://github.com/kalinjul/kotlin-multiplatform-oidc/actions/workflows/build.yml)

Library for using OpenID Connect / OAuth 2.0 in Kotlin Multiplatform (iOS+Android), Android and soon Xcode projects.

- Currently, it only supports the [Authorization Code Grant Flow](https://datatracker.ietf.org/doc/html/rfc6749#section-4.1).
- Support for [discovery](https://openid.net/specs/openid-connect-discovery-1_0.html) via .well-known/openid-configuration.
- Support for [PKCE](https://datatracker.ietf.org/doc/html/rfc7636)

# Usage in Kotlin Multiplatform
Add the dependency to your commonMain sourceSet:
```kotlin
implementation("org.publicvalue.multiplatform.oidc:appsupport:0.0.1")
```

## iOS App usage

Info.plist / Project -> Target -> Info -> URL Types:
Add redirect schema (ex. org.publicvalue.multiplatform.oidc.sample)

## Android App usage

build.gradle.kts:
```
    defaultConfig {
        addManifestPlaceholders(
            mapOf("oidcRedirectScheme" to "<uri scheme>")
        )
    }
```
