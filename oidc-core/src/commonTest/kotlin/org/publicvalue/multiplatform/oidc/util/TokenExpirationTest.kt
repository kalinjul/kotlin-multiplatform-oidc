package org.publicvalue.multiplatform.oidc.util

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

class TokenExpirationTest {

    @Test
    fun accessTokenNotYetExpired() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = 120,
            received_at = now
        )

        assertThat(response.accessTokenExpired(DefaultTokenExpirationPolicy())).isFalse()
    }

    @Test
    fun accessTokenExpiredWithinTolerance() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = 30,
            received_at = now
        )

        assertThat(response.accessTokenExpired(DefaultTokenExpirationPolicy())).isTrue()
    }

    @Test
    fun refreshTokenNotYetExpired() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_token_expires_in = 120,
            received_at = now
        )

        assertThat(response.refreshTokenExpired(DefaultTokenExpirationPolicy())).isFalse()
    }

    @Test
    fun refreshTokenExpiredWithinTolerance() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_token_expires_in = 30,
            received_at = now
        )

        assertThat(response.refreshTokenExpired(DefaultTokenExpirationPolicy())).isTrue()
    }

    @Test
    fun refreshToken_alternativeField_ExpiredWithinTolerance() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_expires_in = 30,
            received_at = now
        )

        assertThat(response.refreshTokenExpired(DefaultTokenExpirationPolicy())).isTrue()
    }

    @Test
    fun accessTokenNeverExpiresWithoutExpiresIn() {
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = null,
            received_at = 0
        )

        assertThat(response.accessTokenExpired(DefaultTokenExpirationPolicy())).isFalse()
    }

    @Test
    fun refreshTokenNeverExpiresWithoutRefreshTokenExpiresIn() {
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_token_expires_in = null,
            received_at = 0
        )

        assertThat(response.refreshTokenExpired(DefaultTokenExpirationPolicy())).isFalse()
    }

    @Test
    fun customToleranceIsRespected() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = 30,
            refresh_token_expires_in = 30,
            received_at = now
        )
        val policy = DefaultTokenExpirationPolicy(expiryTimeTolerance = 10.seconds)

        assertThat(response.accessTokenExpired(policy)).isFalse()
        assertThat(response.refreshTokenExpired(policy)).isFalse()
        assertThat(policy.accessTokenExpired(response)).isFalse()
        assertThat(policy.refreshTokenExpired(response)).isFalse()
    }

    @Test
    fun customPolicyIsUsed() {
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = null,
            received_at = 0
        )
        val policy = object : TokenExpirationPolicy {
            override fun accessTokenExpired(response: AccessTokenResponse) = true
            override fun refreshTokenExpired(response: AccessTokenResponse) = true
        }

        assertThat(response.accessTokenExpired(policy)).isTrue()
        assertThat(response.refreshTokenExpired(policy)).isTrue()
    }
}
