package org.publicvalue.multiplatform.oidc.sample

actual object PlatformConstants : Constants {
    actual override val redirectUrl: String = "http://localhost:8080/redirect"
    actual override val postLogoutRedirectUrl: String = "http://localhost:8080/logout"
}