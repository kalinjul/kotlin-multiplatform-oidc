package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@Serializable
@ObjCName(swiftName = "AccessTokenResponse", name = "AccessTokenResponse", exact = true)
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
