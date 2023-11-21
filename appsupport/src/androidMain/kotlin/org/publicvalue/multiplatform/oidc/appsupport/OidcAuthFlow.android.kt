package org.publicvalue.multiplatform.oidc.appsupport

import android.content.Context
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import org.publicvalue.multiplatform.oidc.OpenIDConnectClient
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

actual class PlatformOidcAuthFlow constructor(
    val context: Context,
    client: OpenIDConnectClient
) : OidcAuthFlow(client) {
    override suspend fun getAccessCode(request: AuthCodeRequest): AuthResponse {
        val authResponse:AuthResponse = suspendCoroutine { continuation ->
            HandleRedirectActivity.currentCallback = {
                continuation.resume(it)
            }

            val builder = CustomTabsIntent.Builder()
            val intent = builder.build()
            intent.launchUrl(context, Uri.parse(request.url.toString()))
        }

        return authResponse
    }
}
