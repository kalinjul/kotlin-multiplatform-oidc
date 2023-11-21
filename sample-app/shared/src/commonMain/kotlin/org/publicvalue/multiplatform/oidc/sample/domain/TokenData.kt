package org.publicvalue.multiplatform.oidc.sample.domain

import kotlinx.serialization.Serializable

@Serializable
data class TokenData(
    val accessToken: String?,
    val refreshToken: String?,
    val expiresIn: Int,
    val issuedAt: Long
)