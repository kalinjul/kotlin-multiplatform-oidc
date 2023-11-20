package org.publicvalue.multiplatform.oidc.sample.domain

data class TokenData(
    val accessToken: String,
    val refreshToken: String,
    val tokenLifetime: String
)