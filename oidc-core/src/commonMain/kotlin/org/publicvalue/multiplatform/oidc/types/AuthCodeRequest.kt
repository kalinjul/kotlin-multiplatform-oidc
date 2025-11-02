package org.publicvalue.multiplatform.oidc.types

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.flows.Pkce
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthCodeRequest", name = "AuthCodeRequest", exact = true)
public data class AuthCodeRequest(
    public val url: Url,
    public val config: OpenIdConnectClientConfig,
    public val pkce: Pkce,
    public val state: String,
    public val nonce: String?
)

public fun AuthCodeRequest.validateState(state: String): Boolean {
    return state == this.state
}

public fun AuthCodeRequest.validateNonce(nonce: String): Boolean {
    return config.disableNonce || nonce == this.nonce
}
