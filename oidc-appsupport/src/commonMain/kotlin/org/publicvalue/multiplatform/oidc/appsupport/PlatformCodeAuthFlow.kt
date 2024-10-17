package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

expect class PlatformCodeAuthFlow: CodeAuthFlow {
    // in kotlin 2.0, we need to implement methods in expect classes
    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse
}