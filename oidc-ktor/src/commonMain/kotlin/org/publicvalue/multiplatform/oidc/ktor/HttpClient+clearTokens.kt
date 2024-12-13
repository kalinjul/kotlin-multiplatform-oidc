package org.publicvalue.multiplatform.oidc.ktor

import io.ktor.client.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.utils.io.core.*

/**
 * Force the Auth plugin to invoke the `loadTokens` block again on the next client request.
 *
 * @see https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death
*/

@Suppress("unused")
fun HttpClient.clearTokens() {
    authProvider<BearerAuthProvider>()?.clearToken()
}