package org.publicvalue.multiplatform.oidc.sample.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class TokenData(
    val accessToken: String?,
    val refreshToken: String?,
    val idToken: String?,
    val expiresIn: Int,
    val issuedAt: Long
)