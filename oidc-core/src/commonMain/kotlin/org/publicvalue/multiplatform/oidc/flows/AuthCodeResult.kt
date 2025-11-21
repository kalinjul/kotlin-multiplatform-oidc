package org.publicvalue.multiplatform.oidc.flows

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.getError
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

typealias AuthCodeResponse = Result<AuthCodeResult>

/**
 * Result of an Auth Code Request
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthCodeResult", name = "AuthCodeResult", exact = true)
@Serializable
data class AuthCodeResult(
    val code: String?,
    val state: String?
)

/**
 * Throws if uri contains an error.
 */
@Throws(OpenIdConnectException::class)
internal fun Url.toAuthCodeResult(): AuthCodeResult {
    getError()?.let { throw it }
    val state = parameters["state"]
    val code = parameters["code"]
    return AuthCodeResult(code, state)
}