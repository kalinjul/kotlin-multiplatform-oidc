package org.publicvalue.multiplatform.oidc.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin

/**
 * Force the Auth plugin to invoke the `loadTokens` block again on the next client request.
 *
 * @see https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death
*/

@Suppress("unused")
fun HttpClient.clearTokens() {
    authProvider<BearerAuthProvider>()?.clearToken()
}