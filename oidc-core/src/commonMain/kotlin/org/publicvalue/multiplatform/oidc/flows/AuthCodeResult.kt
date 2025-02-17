package org.publicvalue.multiplatform.oidc.flows

import kotlinx.serialization.Serializable
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