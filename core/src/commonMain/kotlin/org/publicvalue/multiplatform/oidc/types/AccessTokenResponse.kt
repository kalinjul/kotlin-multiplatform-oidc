package org.publicvalue.multiplatform.oidc.types

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

@Serializable
data class AccessTokenResponse(
    val access_token: String,
    val token_type: String? = null,
    val expires_in: Int? = null,
    val refresh_token: String? = null,
    val refresh_token_expires_in: Int?,
    val id_token: String? = null,
    val scope: String? = null,
    val received_at: Long = Clock.System.now().epochSeconds
) {
}
