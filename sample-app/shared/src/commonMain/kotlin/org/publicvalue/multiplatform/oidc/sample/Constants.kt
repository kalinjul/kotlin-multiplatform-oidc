package org.publicvalue.multiplatform.oidc.sample

internal interface Constants {
    val redirectUrl: String
    val postLogoutRedirectUrl: String
}

internal expect object PlatformConstants : Constants
