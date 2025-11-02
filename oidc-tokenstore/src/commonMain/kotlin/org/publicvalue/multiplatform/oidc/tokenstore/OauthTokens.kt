package org.publicvalue.multiplatform.oidc.tokenstore
@Suppress("ForbiddenPublicDataClass")
public data class OauthTokens(
    val accessToken: String,
    val refreshToken: String?,
    val idToken: String?
)
