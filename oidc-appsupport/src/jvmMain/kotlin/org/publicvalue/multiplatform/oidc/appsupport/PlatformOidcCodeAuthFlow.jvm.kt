package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import io.ktor.http.toURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.AuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthRequest
import org.publicvalue.multiplatform.oidc.types.remote.AuthResponse
import org.publicvalue.multiplatform.oidc.types.remote.AuthResult
import java.awt.Desktop
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

actual class PlatformAuthFlow(
    client: OpenIdConnectClient
) : AuthFlow(client) {
    companion object {
        var PORT = 8080
    }

    override suspend fun getAuthorizationResult(request: AuthRequest): AuthResponse {
        val redirectUrl = request.config.redirectUri?.let { Url(it) }
        if (redirectUrl?.port != PORT || !redirectUrl.isLocalhost()) {
            throw OpenIdConnectException.AuthenticationFailure("JVM implementation can only handle redirect uris using localhost port 8080! Redirect uri was: $redirectUrl")
        }

        val webserver = Webserver()

        val response = withContext(Dispatchers.IO) {
            async {
                request.url.openInBrowser()
                val response = webserver.startAndWaitForRedirect(PORT, redirectPath = redirectUrl.encodedPath)
                webserver.stop()
                response
            }.await()
        }

        val authCode = response?.queryParameters?.get("code")?.ifBlank { null }
        val state = response?.queryParameters?.get("state")?.ifBlank { null }

        if (authCode == null) {
            val accessToken = response?.queryParameters?.get("access_token")?.ifBlank { null }
            if (accessToken != null) {
                return AuthResponse.success(
                    AuthResult.AccessToken(
                        access_token = accessToken,
                        token_type = response.queryParameters["token_type"]?.ifBlank { null },
                        expires_in = response.queryParameters["expires_in"]?.ifBlank { null }?.toIntOrNull()
                    )
                )
            }
        }
        return AuthResponse.success(
            AuthResult.Code(
                code = authCode,
                state = state
            )
        )
    }
}

fun Url.isLocalhost(): Boolean {
    return try {
        val address = InetAddress.getByName(host)
        if (address.isAnyLocalAddress || address.isLoopbackAddress) {
            return true
        }
        try {
            NetworkInterface.getByInetAddress(address) != null
        } catch (e: SocketException) {
            false
        }
    } catch (e: Exception) {
        false
    }
}

fun Url.openInBrowser() {
    val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(toURI())
        } catch (e: Exception) {
            e.printStackTrace()
            throw UrlOpenException(e.message, cause = e)
        }
    } else {
        throw UrlOpenException("Desktop does not support Browse Action")
    }
}

data class UrlOpenException(
    override val message: String?, override val cause: Throwable? = null
): Exception(message, cause)