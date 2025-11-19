package org.publicvalue.multiplatform.oidc.types

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.flows.Pkce
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthCodeRequest", name = "AuthCodeRequest", exact = true)
data class AuthCodeRequest(
    val url: Url,
    val config: OpenIdConnectClientConfig,
    val pkce: Pkce,
    val state: String,
    val nonce: String?
)

fun AuthCodeRequest.validateState(state: String): Boolean {
    return state == this.state
}

fun AuthCodeRequest.validateNonce(nonce: String): Boolean {
    return config.disableNonce || nonce == this.nonce
}