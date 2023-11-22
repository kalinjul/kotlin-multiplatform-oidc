package org.publicvalue.multiplatform.oidc.types

enum class CodeChallengeMethod(
    val queryString: String?
) {
    S256("S256"), plain("plain"), off(null)
}