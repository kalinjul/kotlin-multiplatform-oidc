package org.publicvalue.multiplatform.oidc.flows

import kotlinx.serialization.Serializable
import org.publicvalue.multiplatform.oidc.encodeForPKCE
import org.publicvalue.multiplatform.oidc.s256
import org.publicvalue.multiplatform.oidc.secureRandomBytes
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Proof Key for Code Exchange [RFC7636](https://datatracker.ietf.org/doc/html/rfc7636) implementation.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PKCE", name = "PKCE", exact = true)
@Serializable
data class Pkce(
    /** For token request **/
    val codeVerifier: String = verifier(),
    /** For authorization **/
    val codeChallenge: String
) {
    constructor(codeChallengeMethod: CodeChallengeMethod, codeVerifier: String = verifier()) : this(
        codeChallenge = challenge(codeVerifier = codeVerifier, method = codeChallengeMethod),
        codeVerifier = codeVerifier
    )

    private companion object {
        fun verifier(): String {
            val bytes = secureRandomBytes()
            return bytes.encodeForPKCE()
        }

        fun challenge(codeVerifier: String, method: CodeChallengeMethod): String {
            return if (method == CodeChallengeMethod.S256) codeVerifier.s256().encodeForPKCE() else codeVerifier
        }
    }
}

