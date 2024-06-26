package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import io.ktor.http.toURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import java.awt.Desktop
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException

actual class PlatformCodeAuthFlow(
    client: OpenIdConnectClient
) : CodeAuthFlow(client) {
    companion object {
        var PORT = 8080
    }

    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {

        val redirectUrl = request.config.redirectUri?.let { Url(it) }
        if (redirectUrl?.port != PORT || !redirectUrl.isLocalhost()) {
            throw OpenIdConnectException.AuthenticationFailure("JVM implementation can only handle redirect uris using localhost port 8080! Redirect uri was: $redirectUrl")
        }

        val webserver = Webserver()

        val response =
            withContext(Dispatchers.IO) {
                async {
                    request.url.openInBrowser()
                    val response = webserver.startAndWaitForRedirect(PORT, redirectPath = redirectUrl.encodedPath)
                    webserver.stop()
                    response
                }.await()
            }

        val authCode = response?.queryParameters?.get("code")
        val state = response?.queryParameters?.get("state")

        return AuthCodeResponse.success(
            AuthCodeResult(
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