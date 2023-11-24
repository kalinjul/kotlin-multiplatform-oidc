package org.publicvalue.multiplatform.oidc.flows

typealias AuthCodeResponse = Result<AuthCodeResult>

data class AuthCodeResult(
    val code: String?,
    val state: String?
)