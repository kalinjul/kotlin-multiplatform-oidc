package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class WebSessionFlowTest {

    @Test
    fun httpsRedirectUsesCallbackMatchingWhenAvailable() {
        val callback = Url("https://auth.example.com/auth/callback").httpsCallbackOrNull()

        if (supportsCallbackMatching) {
            assertNotNull(callback, "https redirect uri should use callback matching on iOS 17.4+")
        } else {
            assertNull(callback, "callback matching is unavailable before iOS 17.4")
        }
    }

    @Test
    fun privateUseSchemeFallsBackToSchemeMatching() {
        assertNull(Url("com.example.app://auth/callback").httpsCallbackOrNull())
    }

    @Test
    fun plainHttpFallsBackToSchemeMatching() {
        assertNull(Url("http://localhost:8080/auth/callback").httpsCallbackOrNull())
    }

    @Test
    fun redirectUriIsSplitIntoHostAndPath() {
        val redirect = Url("https://auth.example.com/auth/callback")

        assertEquals("auth.example.com", redirect.host)
        assertEquals("/auth/callback", redirect.encodedPath)
    }
}
