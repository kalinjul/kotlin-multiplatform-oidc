package org.publicvalue.multiplatform.oidc.sample

actual object PlatformConstants : Constants {
    actual override val redirectUrl: String = "org.publicvalue.multiplatform.oidc.sample://redirect"
    actual override val postLogoutRedirectUrl: String = "org.publicvalue.multiplatform.oidc.sample://logout"
}