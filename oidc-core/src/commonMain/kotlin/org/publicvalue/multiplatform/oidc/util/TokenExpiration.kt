package org.publicvalue.multiplatform.oidc.util

import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class TokenExpirationPolicy(
    /**
     * Tokens with less this duration left before expiration are considered expired.
     */
    val expiryTimeTolerance: Duration = 60.seconds
)

val DefaultTokenExpirationPolicy: TokenExpirationPolicy = TokenExpirationPolicy()

val AccessTokenResponse.accessTokenExpirationTime: Instant? get() {
    return expires_in?.let { Instant.fromEpochSeconds(received_at + it) }
}

val AccessTokenResponse.refreshTokenExpirationTime: Instant? get() {
    return (refresh_token_expires_in ?: refresh_expires_in)?.let { Instant.fromEpochSeconds(received_at + it) }
}

fun AccessTokenResponse.accessTokenExpired(expiryTimeTolerance: Duration = DefaultTokenExpirationPolicy.expiryTimeTolerance): Boolean = accessTokenExpirationTime?.let { it <= Clock.System.now() + expiryTimeTolerance } ?: false
fun AccessTokenResponse.refreshTokenExpired(expiryTimeTolerance: Duration = DefaultTokenExpirationPolicy.expiryTimeTolerance): Boolean = refreshTokenExpirationTime?.let { it <= Clock.System.now() + expiryTimeTolerance } ?: false

