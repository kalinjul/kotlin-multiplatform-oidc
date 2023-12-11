package org.publicvalue.multiplatform.oidc.flows

import org.publicvalue.multiplatform.oidc.encodeForPKCE
import org.publicvalue.multiplatform.oidc.randomBytes
import org.publicvalue.multiplatform.oidc.s256
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Proof Key for Code Exchange [RFC7636](https://datatracker.ietf.org/doc/html/rfc7636) implementation.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PKCE", name = "PKCE", exact = true)
class Pkce(
    codeChallengeMethod: CodeChallengeMethod,
    /** For token request **/
    val codeVerifier: String = verifier(),
    /** For authorization **/
    val codeChallenge: String = challenge(codeVerifier, codeChallengeMethod),
) {
    private companion object {
        fun verifier(): String {
            val bytes = randomBytes()
            return bytes.encodeForPKCE()
        }

        fun challenge(codeVerifier: String, method: CodeChallengeMethod): String {
            return if (method == CodeChallengeMethod.S256) codeVerifier.s256().encodeForPKCE() else codeVerifier
        }
    }
}

