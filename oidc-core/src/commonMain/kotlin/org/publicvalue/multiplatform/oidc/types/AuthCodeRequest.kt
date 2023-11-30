package org.publicvalue.multiplatform.oidc.types

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIDConnectClientConfig
import org.publicvalue.multiplatform.oidc.flows.PKCE
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthCodeRequest", name = "AuthCodeRequest", exact = true)
data class AuthCodeRequest(
    val url: Url,
    val config: OpenIDConnectClientConfig,
    val pkce: PKCE,
    val state: String,
    val nonce: String
)

fun AuthCodeRequest.validateState(state: String): Boolean {
    return state == this.state
}