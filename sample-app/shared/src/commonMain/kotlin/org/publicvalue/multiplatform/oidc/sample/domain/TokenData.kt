package org.publicvalue.multiplatform.oidc.sample.domain

data class TokenData(
    val token: String,
    val refreshToken: String,
    val tokenLifetime: String
)