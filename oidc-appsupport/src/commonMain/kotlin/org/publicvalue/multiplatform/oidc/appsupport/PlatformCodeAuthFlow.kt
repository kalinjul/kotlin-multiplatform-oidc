package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

expect class PlatformCodeAuthFlow: CodeAuthFlow {

    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse
}