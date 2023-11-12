package org.publicvalue.multiplatform.oidc.discovery

import kotlinx.serialization.Serializable

@Serializable
data class OpenIDConnectConfiguration(
    val authorization_endpoint: String?,
    val token_endpoint: String?,
    val device_authorization_endpoint: String?,
    val userinfo_endpoint: String?,
    val end_session_endpoint: String?,
    val introspection_endpoint: String?,

    val issuer: String?,
    val jwks_uri: String?,
    val response_types_supported: List<String>?,
    val id_token_signing_alg_values_supported: List<String>?,
    val frontchannel_logout_supported: Boolean?,
    val scopes_supported: List<String>?,
    val claims_supported: List<String>?,
    val subject_types_supported: List<String>?,
    val token_endpoint_auth_methods_supported: List<String>?,
    val grant_types_supported: List<String>?,
    val introspection_endpoint_auth_methods_supported: List<String>?,
) {

}