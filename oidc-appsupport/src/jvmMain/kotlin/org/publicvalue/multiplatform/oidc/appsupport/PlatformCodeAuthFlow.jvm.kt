package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SslWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResponse
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult
import org.publicvalue.multiplatform.oidc.flows.CodeAuthFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.EndSessionResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import java.awt.Desktop
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalOpenIdConnect
actual class PlatformCodeAuthFlow(
    actual override val client: OpenIdConnectClient,
    private val webserverProvider: () -> Webserver = { SimpleKtorWebserver() },
    openUrl: (Url) -> Unit = { it.openInBrowser() },
    private val port: Int
) : CodeAuthFlow, EndSessionFlow {

    private val webFlow = WebServerFlow(
        webserverProvider = webserverProvider,
        openUrl = openUrl,
    )

    actual override suspend fun getAuthorizationCode(request: AuthCodeRequest): AuthCodeResponse {
        val redirectUrl = request.config.redirectUri?.let { Url(it) }
        checkRedirectPort(redirectUrl)

        val result = webFlow.startWebFlow(request.url, redirectUrl, port)

        val code = result.parameters["code"]
        val state = result.parameters["state"]
        return AuthCodeResponse.success(AuthCodeResult(code, state))
    }

    actual override suspend fun endSession(request: EndSessionRequest): EndSessionResponse {
        val redirectUrl = Url(request.url.parameters.get("post_logout_redirect_uri").orEmpty())
        checkRedirectPort(redirectUrl)

        webFlow.startWebFlow(request.url, redirectUrl, port)
        return EndSessionResponse.success(Unit)
    }

    @OptIn(ExperimentalContracts::class)
    private fun checkRedirectPort(redirectUrl: Url?) {
        contract {
            returns() implies (redirectUrl != null)
        }
        
        if (redirectUrl == null) {
            throw OpenIdConnectException.AuthenticationFailure("Redirect URL cannot be null")
        }
        
        // Check if the port matches
        if (redirectUrl.port != port) {
            throw OpenIdConnectException.AuthenticationFailure(
                "JVM implementation can only handle redirect uris using port $port! Redirect uri was: $redirectUrl"
            )
        }
        
        // Check if it's localhost
        if (!redirectUrl.isLocalhost()) {
            throw OpenIdConnectException.AuthenticationFailure(
                "JVM implementation can only handle redirect uris using localhost! Redirect uri was: $redirectUrl"
            )
        }
        
        // Check protocol compatibility with webserver
        val expectedProtocol = getExpectedProtocol()
        if (redirectUrl.protocol.name.lowercase() != expectedProtocol) {
            throw OpenIdConnectException.AuthenticationFailure(
                "Redirect URL protocol '${redirectUrl.protocol.name}' does not match webserver protocol '$expectedProtocol'. " +
                "Redirect uri was: $redirectUrl"
            )
        }
    }
    
    /**
     * Get the expected protocol based on the webserver configuration
     */
    private fun getExpectedProtocol(): String {
        return try {
            val webserver = webserverProvider()
            when (webserver) {
                is SslWebserver -> if (webserver.isHttpsEnabled) "https" else "http"
                else -> "http" // Default to HTTP for other webserver types
            }
        } catch (e: Exception) {
            // If we can't determine the webserver type, default to HTTP
            "http"
        }
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