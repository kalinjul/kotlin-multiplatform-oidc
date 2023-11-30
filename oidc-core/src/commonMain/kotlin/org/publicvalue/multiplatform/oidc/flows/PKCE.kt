package org.publicvalue.multiplatform.oidc.flows

import org.publicvalue.multiplatform.oidc.encodeForPKCE
import org.publicvalue.multiplatform.oidc.randomBytes
import org.publicvalue.multiplatform.oidc.s256
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "PKCE", name = "PKCE", exact = true)
class PKCE(
    codeChallengeMethod: CodeChallengeMethod,
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
            return if (method == CodeChallengeMethod.S256) codeVerifier.s256().encodeForPKCE() else codeVerifier
        }

    }
}

