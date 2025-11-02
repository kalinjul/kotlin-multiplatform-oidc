package org.publicvalue.multiplatform.oidc.types

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Code Challenge Methods defined by [RFC7636: PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "CodeChallengeMethod", name = "CodeChallengeMethod", exact = true)
public enum class CodeChallengeMethod(
    public val queryString: String?
) {
    /** Send a random code_challenge in code request and a SHA-256 hashed code_verifier in token
     *  request.
     **/
    S256("S256"),

    /**
     *  code_challenge = code_verifier
     */
    @Suppress("unused")
    PLAIN("plain"),

    /**
     * Disable PKCE Headers.
     */
    OFF(null)
}
