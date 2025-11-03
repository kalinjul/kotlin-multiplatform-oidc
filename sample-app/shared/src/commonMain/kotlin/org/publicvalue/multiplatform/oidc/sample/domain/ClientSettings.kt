package org.publicvalue.multiplatform.oidc.sample.domain

import kotlinx.serialization.Serializable
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

@Serializable
internal data class ClientSettings(
    val name: String? = null,
    val client_id: String? = null,
    val client_secret: String? = null,
    val scope: String? = null,
    val code_challenge_method: CodeChallengeMethod = CodeChallengeMethod.S256,
) {
    companion object {
        val Empty = ClientSettings(
            scope = "openid profile"
        )
    }

    fun isValid(): Boolean {
        return client_id != null && scope != null
    }
}