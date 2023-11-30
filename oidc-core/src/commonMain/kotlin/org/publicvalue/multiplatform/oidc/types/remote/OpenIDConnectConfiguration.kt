package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Openid-configuration JSON
 */
@OptIn(ExperimentalObjCName::class)
@Serializable
@ObjCName(swiftName = "OpenIDConnectConfiguration")
data class OpenIDConnectConfiguration(
    val authorization_endpoint: String? = null,
    val token_endpoint: String? = null,
    val device_authorization_endpoint: String? = null,
    val userinfo_endpoint: String? = null,
    val end_session_endpoint: String? = null,
    val introspection_endpoint: String? = null,

    val issuer: String? = null,
    val jwks_uri: String? = null,
    val response_types_supported: List<String>? = null,
    val id_token_signing_alg_values_supported: List<String>? = null,
    val frontchannel_logout_supported: Boolean? = null,
    val scopes_supported: List<String>? = null,
    val claims_supported: List<String>? = null,
    val subject_types_supported: List<String>? = null,
    val token_endpoint_auth_methods_supported: List<String>? = null,
    val grant_types_supported: List<String>? = null,
    val introspection_endpoint_auth_methods_supported: List<String>? = null,
)