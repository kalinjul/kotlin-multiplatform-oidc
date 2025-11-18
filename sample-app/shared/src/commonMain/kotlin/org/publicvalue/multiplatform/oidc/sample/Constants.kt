package org.publicvalue.multiplatform.oidc.sample

interface Constants {
    val redirectUrl: String
    val postLogoutRedirectUrl: String
}

expect object PlatformConstants : Constants {
    override val redirectUrl: String
    override val postLogoutRedirectUrl: String
}