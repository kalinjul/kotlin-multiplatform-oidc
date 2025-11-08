package org.publicvalue.multiplatform.oidc.sample.domain

import kotlinx.serialization.Serializable

@Serializable
internal data class IdpSettings(
    val discoveryUrl: String? = null,
    val endpointToken: String? = null,
    val endpointAuthorization: String? = null,
    val endpointDeviceAuthorization: String? = null,
    val endpointEndSession: String? = null,
    val endpointUserInfo: String? = null,
    val endpointIntrospection: String? = null,
) {
    companion object {
        val Empty = IdpSettings()
    }

    fun isValid(): Boolean {
        return endpointToken != null && endpointAuthorization != null
    }
}
