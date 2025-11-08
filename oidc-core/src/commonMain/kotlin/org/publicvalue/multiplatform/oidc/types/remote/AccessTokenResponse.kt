package org.publicvalue.multiplatform.oidc.types.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Access Token Response expected from token endpoint.
 *
 * [https://openid.net/specs/openid-connect-core-1_0.html#TokenResponse](https://openid.net/specs/openid-connect-core-1_0.html#TokenResponse)
 * [https://datatracker.ietf.org/doc/html/rfc6749#section-5.1](https://datatracker.ietf.org/doc/html/rfc6749#section-5.1)
 */
@OptIn(ExperimentalObjCName::class, ExperimentalTime::class)
@Serializable
@ObjCName(swiftName = "AccessTokenResponse", name = "AccessTokenResponse", exact = true)
public data class AccessTokenResponse(
    /**
     * **Required**
     *
     * The access token issued by the authorization server.
     */
    @SerialName("access_token")
    val accessToken: String,

    /**
     * **Required**
     *
     * The value MUST be _Bearer_ or another token_type value
     * that the Client has negotiated with the Authorization Server.
     *
     * Clients implementing this profile MUST support the OAuth 2.0 Bearer Token Usage
     * [RFC6750](https://openid.net/specs/openid-connect-core-1_0.html#RFC6750) specification.
     */
    @SerialName("token_type")
    val tokenType: String? = null,

    /**
     * **Recommended**
     *
     * The lifetime in seconds of the [accessToken].
     * For example, the value "3600" denotes that the access token will
     * expire in one hour from the time the response was generated.
     *
     * If omitted, the authorization server SHOULD provide the
     * expiration time via other means or document the default value.
     */
    @SerialName("expires_in")
    val expiresIn: Int? = null,

    /**
     * **Optional**
     *
     * The refresh token, which can be used to obtain new access tokens using the same authorization grant type.
     */
    @SerialName("refresh_token")
    val refreshToken: String? = null,

    /**
     * **Not specified in OAuth 2.0**
     *
     * The lifetime in seconds of the [refreshToken].
     * For example, the value "3600" denotes that the refresh token will
     * expire in one hour from the time the response was generated.
     */
    @SerialName("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int? = null,

    /**
     * **Required in OpenIDConnect**
     *
     * ID Token value associated with the authenticated session.
     */
    @SerialName("id_token")
    val idToken: String? = null,

    /** Optional if identical to request **/
    val scope: String? = null,

    /** Computed locally **/
    @SerialName("received_at")
    val receivedAt: Long = Clock.System.now().epochSeconds // .System.now().epochSeconds
)
