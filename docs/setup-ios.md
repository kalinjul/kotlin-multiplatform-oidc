# iOS Project setup
iOS does not require declaring the redirect scheme.

## AuthFlowFactory
For the iOS part, use [IosCodeAuthFlowFactory](https://kalinjul.github.io/kotlin-multiplatform-oidc/kotlin-multiplatform-oidc/org.publicvalue.multiplatform.oidc.appsupport/-ios-code-auth-flow-factory/index.html).

## Https Redirect Url
If you want to use [https redirect links instead of custom schemes](https://github.com/kalinjul/kotlin-multiplatform-oidc/issues/46),
just configure a `redirectUri` using the https scheme. Host and path of the redirect uri are then matched by ```ASWebAuthenticationSession```.

- This requires iOS 17.4 or later. On older versions, only custom uri schemes will work.
- Your app needs an [associated domain](https://developer.apple.com/documentation/xcode/supporting-associated-domains) for the host of the redirect uri.
