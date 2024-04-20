package org.publicvalue.multiplatform.oidc.flows

import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Objective-C convenience function
 * @see CodeAuthFlow.getAccessToken
 */
@Throws(OpenIdConnectException::class, CancellationException::class)
suspend fun CodeAuthFlow.getAccessToken() = getAccessToken(null)