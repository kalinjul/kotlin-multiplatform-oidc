package org.publicvalue.multiplatform.oidc.appsupport.helper

import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.appsupport.PlatformCodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.coroutines.cancellation.CancellationException

@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun PlatformCodeAuthFlow.getAccessToken(
): AccessTokenResponse {
    startLogin()
    return continueLogin()
}