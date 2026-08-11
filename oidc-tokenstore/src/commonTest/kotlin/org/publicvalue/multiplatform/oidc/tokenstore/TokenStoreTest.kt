package org.publicvalue.multiplatform.oidc.tokenstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.test.Test

@OptIn(ExperimentalOpenIdConnect::class)
class TokenStoreTest {

    private val settings = InMemorySettingsStore()
    private val tokenStore: TokenStore = SettingsTokenStore(settings = settings)

    @Test
    fun saveRestore() = runTest {
        tokenStore.saveTokens("1", "2", "3")

        assertThat(tokenStore.getAccessToken()).isEqualTo("1")
        assertThat(tokenStore.getRefreshToken()).isEqualTo("2")
        assertThat(tokenStore.getIdToken()).isEqualTo("3")
    }

    @Test
    fun saveRestoreFullResponse() = runTest {
        val response = AccessTokenResponse(
            access_token = "1",
            token_type = "Bearer",
            expires_in = 3600,
            refresh_token = "2",
            id_token = "3",
            scope = "openid",
            received_at = 1000L
        )
        tokenStore.saveTokens(response)

        assertThat(tokenStore.getTokenResponse()).isEqualTo(response)
    }

    @Test
    fun removeAll() = runTest {
        tokenStore.saveTokens("1", "2", "3")
        tokenStore.removeTokens()

        assertThat(tokenStore.getAccessToken()).isNull()
        assertThat(tokenStore.getRefreshToken()).isNull()
        assertThat(tokenStore.getIdToken()).isNull()
    }

    @Test
    fun migrateLegacyTokens() = runTest {
        settings.put(SettingsKey.ACCESSTOKEN.name, "1")
        settings.put(SettingsKey.REFRESHTOKEN.name, "2")
        settings.put(SettingsKey.IDTOKEN.name, "3")

        assertThat(tokenStore.getAccessToken()).isEqualTo("1")
        assertThat(tokenStore.getRefreshToken()).isEqualTo("2")
        assertThat(tokenStore.getIdToken()).isEqualTo("3")

        // legacy entries are replaced by the json entry
        assertThat(settings.get(SettingsKey.TOKENS.name)).isNotNull()
        assertThat(settings.get(SettingsKey.ACCESSTOKEN.name)).isNull()
        assertThat(settings.get(SettingsKey.REFRESHTOKEN.name)).isNull()
        assertThat(settings.get(SettingsKey.IDTOKEN.name)).isNull()
    }

    @Test
    fun migrateLegacyAccessTokenOnly() = runTest {
        settings.put(SettingsKey.ACCESSTOKEN.name, "1")

        assertThat(tokenStore.getAccessToken()).isEqualTo("1")
        assertThat(tokenStore.getRefreshToken()).isNull()
        assertThat(tokenStore.getIdToken()).isNull()
    }

    @Test
    fun emptyStore() = runTest {
        assertThat(tokenStore.getTokenResponse()).isNull()
        assertThat(tokenStore.getAccessToken()).isNull()
    }
}
