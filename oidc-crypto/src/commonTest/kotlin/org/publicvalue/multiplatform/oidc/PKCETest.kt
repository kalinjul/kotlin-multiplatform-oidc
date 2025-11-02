package org.publicvalue.multiplatform.oidc

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.publicvalue.multiplatform.oidc.flows.Pkce
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PKCETest {
    @Test
    fun testExample1() {
        val inputBytes = ubyteArrayOf(3u, 236u, 255u, 224u, 193u)

        val result = inputBytes.toByteArray().encodeForPKCE()
        assertThat(result).isEqualTo("A-z_4ME")
    }

    @Test
    fun testExamples() {
        val bytes = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk".s256()
        assertThat(bytes).isEqualTo(
            ubyteArrayOf(
                19u, 211u, 30u, 150u, 26u, 26u, 216u, 236u, 47u, 22u, 177u, 12u, 76u, 152u, 46u,
                8u, 118u, 168u, 120u, 173u, 109u, 241u, 68u, 86u, 110u, 225u, 137u, 74u, 203u,
                112u, 249u, 195u
            ).toByteArray()
        )

        assertThat(
            "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk".s256().encodeForPKCE()
        ).isEqualTo("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM")

        assertThat(
            "OtVkSmgv4FG0Yunzad_Y41FfqBMeNv5R5t9Y1F0SCa4".s256().encodeForPKCE()
        ).isEqualTo("6O-tieQY_-8QPlx2gHJf-kY8ZZsndw8WrYYFheRk4Uk")
        assertThat(
            "49f28fec29cfc6243be4348e20dbb3c45a5ca230dbd2508b61d3bb80".s256().encodeForPKCE()
        ).isEqualTo("71_p-2XnQNtp0ejBitMKotd0KXC4mOrBGQCrV2NB_SM")
        assertThat(
            "gdFYQb_89x1lV7c0oHoCkJEQjRXhQzGRD4kmzYk1s5M".s256().encodeForPKCE()
        ).isEqualTo("t0hcawUItzlHvJczV9dDP18E--htlBE1MlBd37OgZRc")
        assertThat(
            "FvquH4N_qH-YZRxkkfauVCXv0nWmLtbLjnAqQS5LVVU".s256().encodeForPKCE()
        ).isEqualTo("QkfMjldvTK49kFrHLjwm1ZIy1SHkmKong4BmKBATIL0")
        assertThat(
            "12W5Fscr8w34Q6510F8MmiDoN3cBhA_XSWSONObhKUg".s256().encodeForPKCE()
        ).isEqualTo("vPGsmc1pTnazk-Q9lwMOBrzG2wNdEa8KkYoVXq7Tknk")
    }

    @Test
    fun `should generate valid code verifier`() {
        val pkce = Pkce(codeChallengeMethod = CodeChallengeMethod.S256)

        val codeVerifier = pkce.codeVerifier

        assertTrue("Code verifier length must be between 43 and 128") {
            codeVerifier.length in 43..128
        }
        assertTrue("Code verifier must match Base64 URL format") {
            codeVerifier.matches(Regex("^[A-Za-z0-9-_]+$"))
        }
    }

    @Test
    fun `should generate valid code challenge from code verifier`() {
        val pkse = Pkce(codeChallengeMethod = CodeChallengeMethod.S256)
        val codeVerifier = pkse.codeVerifier
        val codeChallenge = codeVerifier.s256().encodeForPKCE()

        assertNotNull(codeChallenge, "Code challenge should not be null",)
        assertTrue("Code challenge must match Base64 URL format") {
            codeChallenge.matches(Regex("^[A-Za-z0-9-_]+$"))
        }
    }
}
