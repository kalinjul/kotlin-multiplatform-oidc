package org.publicvalue.multiplatform.oidc.sample

actual object PlatformConstants : Constants {
    override val redirectUrl: String = "org.publicvalue.multiplatform.oidc.sample://redirect"
    override val postLogoutRedirectUrl: String = "org.publicvalue.multiplatform.oidc.sample://logout"
}