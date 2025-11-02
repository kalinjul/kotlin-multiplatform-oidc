package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Openid-configuration JSON expected from discovery endpoint
 */
@OptIn(ExperimentalObjCName::class)
@Serializable
@ObjCName(swiftName = "OpenIdConnectConfiguration")
public data class OpenIdConnectConfiguration(
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String? = null,
    @SerialName("token_endpoint")
    val tokenEndpoint: String? = null,
    @SerialName("device_authorization_endpoint")
    val deviceAuthorizationEndpoint: String? = null,
    @SerialName("userinfo_endpoint")
    val userinfoEndpoint: String? = null,
    @SerialName("end_session_endpoint")
    val endSessionEndpoint: String? = null,
    @SerialName("introspection_endpoint")
    val introspectionEndpoint: String? = null,
    @SerialName("revocation_endpoint")
    val revocationEndpoint: String? = null,

    val issuer: String? = null,
    @SerialName("jwks_uri")
    val jwksUri: String? = null,
    @SerialName("response_types_supported")
    val responseTypesSupported: List<String>? = null,
    @SerialName("id_token_signing_alg_values_supported")
    val idTokenSigningAlgValuesSupported: List<String>? = null,
    @SerialName("frontchannel_logout_supported")
    val frontChannelLogoutSupported: Boolean? = null,
    @SerialName("scopes_supported")
    val scopesSupported: List<String>? = null,
    @SerialName("claims_supported")
    val claimsSupported: List<String>? = null,
    @SerialName("subject_types_supported")
    val subjectTypesSupported: List<String>? = null,
    @SerialName("token_endpoint_auth_methods_supported")
    val tokenEndpointAuthMethodsSupported: List<String>? = null,
    @SerialName("grant_types_supported")
    val grantTypesSupported: List<String>? = null,
    @SerialName("introspection_endpoint_auth_methods_supported")
    val introspectionEndpointAuthMethodsSupported: List<String>? = null,
)
