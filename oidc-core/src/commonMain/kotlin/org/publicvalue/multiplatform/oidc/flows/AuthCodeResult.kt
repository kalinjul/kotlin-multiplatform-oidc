package org.publicvalue.multiplatform.oidc.flows

import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

public typealias AuthCodeResponse = Result<AuthCodeResult>
public typealias EndSessionResponse = Result<Unit>

/**
 * Result of an Auth Code Request
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName(swiftName = "AuthCodeResult", name = "AuthCodeResult", exact = true)
@Serializable
public data class AuthCodeResult(
    val code: String?,
    val state: String?
)
