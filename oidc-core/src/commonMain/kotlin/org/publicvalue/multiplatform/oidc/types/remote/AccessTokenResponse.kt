package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * https://openid.net/specs/openid-connect-core-1_0.html#TokenResponse
 * https://datatracker.ietf.org/doc/html/rfc6749#section-5.1
 */
@OptIn(ExperimentalObjCName::class)
@Serializable
@ObjCName(swiftName = "AccessTokenResponse", name = "AccessTokenResponse", exact = true)
data class AccessTokenResponse(
    /** Required **/
    val access_token: String,
    /** Required **/
    val token_type: String? = null,
    /** Recommended **/
    val expires_in: Int? = null,
    /** Optional **/
    val refresh_token: String? = null,
    /** Not specified in OAuth 2.0 **/
    val refresh_token_expires_in: Int? = null,
    /** Required in OpenIDConnect **/
    val id_token: String? = null,
    /** Optional if identical to request **/
    val scope: String? = null,
    /** Computed locally **/
    val received_at: Long = Clock.System.now().epochSeconds
)
