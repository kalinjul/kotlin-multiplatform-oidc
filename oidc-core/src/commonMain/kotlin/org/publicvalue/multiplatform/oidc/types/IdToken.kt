package org.publicvalue.multiplatform.oidc.types

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * https://openid.net/specs/openid-connect-core-1_0.html#IDToken
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("IdToken", "IdToken", exact = true)
public data class IdToken(
    /** Required: Issuer (must match the discovery document) **/
    val iss: String?,
    /** Required: Subject identifier **/
    val sub: String?,
    /** Required: Audience (must contain client_id) **/
    val aud: List<String>?,
    /** Required: Expiration time **/
    val exp: Long?,
    /** Required: Issued at **/
    val iat: Long?,
    /** Optional time of user auth **/
    val authTime: Long?,
    /** Optional, if present, must match request nonce **/
    val nonce: String?,
    /** Optional: Authentication Context Class Reference **/
    val acr: String?,

    /** Optional: Authentication Methods References**/
    val amr: List<String>?,
    /** Optional: Authorized Party **/
    val azp: String?,

    val alg: String?,
    val kid: String?,
    /** Optional: Access Token hash **/
    val atHash: String?,

    val additionalClaims: Map<String, Any?>
)
