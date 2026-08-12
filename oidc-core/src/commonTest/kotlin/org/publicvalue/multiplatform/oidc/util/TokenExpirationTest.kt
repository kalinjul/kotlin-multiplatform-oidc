package org.publicvalue.multiplatform.oidc.util

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.test.Test
import kotlin.time.Clock

class TokenExpirationTest {

    @Test
    fun accessTokenNotYetExpired() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = 120,
            received_at = now
        )

        assertThat(response.accessTokenExpired()).isFalse()
    }

    @Test
    fun accessTokenExpiredWithinTolerance() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = 30,
            received_at = now
        )

        assertThat(response.accessTokenExpired()).isTrue()
    }

    @Test
    fun refreshTokenNotYetExpired() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_token_expires_in = 120,
            received_at = now
        )

        assertThat(response.refreshTokenExpired()).isFalse()
    }

    @Test
    fun refreshTokenExpiredWithinTolerance() {
        val now = Clock.System.now().epochSeconds
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_token_expires_in = 30,
            received_at = now
        )

        assertThat(response.refreshTokenExpired()).isTrue()
    }

    @Test
    fun accessTokenNeverExpiresWithoutExpiresIn() {
        val response = AccessTokenResponse(
            access_token = "access",
            expires_in = null,
            received_at = 0
        )

        assertThat(response.accessTokenExpired()).isFalse()
    }

    @Test
    fun refreshTokenNeverExpiresWithoutRefreshTokenExpiresIn() {
        val response = AccessTokenResponse(
            access_token = "access",
            refresh_token_expires_in = null,
            received_at = 0
        )

        assertThat(response.refreshTokenExpired()).isFalse()
    }
}
