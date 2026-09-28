package org.publicvalue.multiplatform.oidc.util

import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import org.publicvalue.multiplatform.oidc.util.TokenExpirationPolicy.Companion.DEFAULT_EXPIRY_TIME_TOLERANCE
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

interface TokenExpirationPolicy {
    companion object {
        val DEFAULT_EXPIRY_TIME_TOLERANCE: Duration = 60.seconds
    }
    fun accessTokenExpired(response: AccessTokenResponse): Boolean
    fun refreshTokenExpired(response: AccessTokenResponse): Boolean
}

class DefaultTokenExpirationPolicy(
    /**
     * Tokens with less this duration left before expiration are considered expired.
     */
    val expiryTimeTolerance: Duration = DEFAULT_EXPIRY_TIME_TOLERANCE
): TokenExpirationPolicy {
    override fun accessTokenExpired(response: AccessTokenResponse): Boolean {
        return response.accessTokenExpirationTime?.let { it <= Clock.System.now() + expiryTimeTolerance } ?: false
    }

    override fun refreshTokenExpired(response: AccessTokenResponse): Boolean {
        return response.refreshTokenExpirationTime?.let { it <= Clock.System.now() + expiryTimeTolerance } ?: false
    }
}

val AccessTokenResponse.accessTokenExpirationTime: Instant? get() {
    return expires_in?.let { Instant.fromEpochSeconds(received_at + it) }
}

val AccessTokenResponse.refreshTokenExpirationTime: Instant? get() {
    return (refresh_token_expires_in ?: refresh_expires_in)?.let { Instant.fromEpochSeconds(received_at + it) }
}

@Deprecated("Use accessTokenExpired(expirationPolicy) instead", replaceWith = ReplaceWith("accessTokenExpired()"))
fun AccessTokenResponse.accessTokenExpired(expiryTimeTolerance: Duration): Boolean = accessTokenExpirationTime?.let { it <= Clock.System.now() + expiryTimeTolerance } ?: false
@Deprecated("Use refreshTokenExpired(expirationPolicy) instead", replaceWith = ReplaceWith("refreshTokenExpired()"))
fun AccessTokenResponse.refreshTokenExpired(expiryTimeTolerance: Duration): Boolean = refreshTokenExpirationTime?.let { it <= Clock.System.now() + expiryTimeTolerance } ?: false

fun AccessTokenResponse.accessTokenExpired(expirationPolicy: TokenExpirationPolicy = DefaultTokenExpirationPolicy()): Boolean = expirationPolicy.accessTokenExpired(this)
fun AccessTokenResponse.refreshTokenExpired(expirationPolicy: TokenExpirationPolicy = DefaultTokenExpirationPolicy()): Boolean = expirationPolicy.refreshTokenExpired(this)

