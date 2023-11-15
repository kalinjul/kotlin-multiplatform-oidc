package org.publicvalue.multiplatform.oidc.flows

import io.ktor.util.encodeBase64
import org.publicvalue.multiplatform.oidc.encodeForPKCE
import org.publicvalue.multiplatform.oidc.randomBytes
import org.publicvalue.multiplatform.oidc.s256
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.random.Random

class PKCE(
    val codeChallengeMethod: CodeChallengeMethod,
    /** For token request **/
    val codeVerifier: String = verifier(),
    /** For authorization **/
    val codeChallenge: String = challenge(codeVerifier, codeChallengeMethod),
) {
    companion object {
        internal fun verifier(): String {
            val bytes = randomBytes()
            return bytes.encodeForPKCE()
        }

        internal fun challenge(codeVerifier: String, method: CodeChallengeMethod): String {
            return if (method == CodeChallengeMethod.S256) codeVerifier else codeVerifier.s256().encodeForPKCE()
        }

    }
}

