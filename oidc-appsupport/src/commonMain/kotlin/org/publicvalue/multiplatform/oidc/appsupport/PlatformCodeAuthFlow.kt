package org.publicvalue.multiplatform.oidc.appsupport

import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

expect class PlatformCodeAuthFlow: CodeAuthFlow, EndSessionFlow {
    // in kotlin 2.0, we need to implement methods in expect classes
    override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse
    override suspend fun endSession(request: EndSessionRequest): EndSessionResponse
    override val client: OpenIdConnectClient
}