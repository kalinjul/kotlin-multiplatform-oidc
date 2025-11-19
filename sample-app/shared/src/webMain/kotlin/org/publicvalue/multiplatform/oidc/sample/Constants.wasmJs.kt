package org.publicvalue.multiplatform.oidc.sample

actual object PlatformConstants : Constants {
    override val redirectUrl: String = "http://localhost:8080/redirect"
    override val postLogoutRedirectUrl: String = "http://localhost:8080/logout"
}