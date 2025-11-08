package org.publicvalue.multiplatform.oidc.tokenstore

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import kotlinx.coroutines.test.runTest
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import kotlin.test.Test

@OptIn(ExperimentalOpenIdConnect::class)
class TokenStoreTest {

    private val tokenStore: TokenStore = SettingsTokenStore(settings = InMemorySettingsStore())

    @Test
    fun saveRestore() = runTest {
        tokenStore.saveTokens("1", "2", "3")

        assertThat(tokenStore.getAccessToken()).isEqualTo("1")
        assertThat(tokenStore.getRefreshToken()).isEqualTo("2")
        assertThat(tokenStore.getIdToken()).isEqualTo("3")
    }

    @Test
    fun removeOne() = runTest {
        tokenStore.saveTokens("1", "2", "3")
        tokenStore.removeAccessToken()

        assertThat(tokenStore.getAccessToken()).isNull()
        assertThat(tokenStore.getRefreshToken()).isEqualTo("2")
        assertThat(tokenStore.getIdToken()).isEqualTo("3")
    }
}
