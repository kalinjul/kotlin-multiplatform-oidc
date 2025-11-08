package org.publicvalue.multiplatform.oidc.sample.domain

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod

@Serializable
internal data class ClientSettings(
    val name: String? = null,
    @SerialName("client_id")
    val clientId: String? = null,
    @SerialName("client_secret")
    val clientSecret: String? = null,
    val scope: String? = null,
    @SerialName("code_challenge_method")
    val codeChallengeMethod: CodeChallengeMethod = CodeChallengeMethod.S256,
) {
    companion object {
        val Empty = ClientSettings(
            scope = "openid profile"
        )
    }

    fun isValid(): Boolean {
        return clientId != null && scope != null
    }
}
