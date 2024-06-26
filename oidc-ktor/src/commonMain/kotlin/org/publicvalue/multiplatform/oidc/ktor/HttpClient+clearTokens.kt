package org.publicvalue.multiplatform.oidc.ktor

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.plugin
import io.ktor.utils.io.core.*

// https://youtrack.jetbrains.com/issue/KTOR-4759/Auth-BearerAuthProvider-caches-result-of-loadToken-until-process-death
// Force the Auth plugin to invoke the `loadTokens` block again on the next client request.
fun HttpClient.clearTokens() {
    try {
        authProviders
        .filterIsInstance<BearerAuthProvider>()
        .singleOrNull()?.clearToken()
    } catch (e: IllegalStateException) {
    // No-op; plugin not installed
    }
}