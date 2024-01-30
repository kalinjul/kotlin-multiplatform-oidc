package org.publicvalue.multiplatform.oidc.tokenstore

data class OauthTokens(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?
)