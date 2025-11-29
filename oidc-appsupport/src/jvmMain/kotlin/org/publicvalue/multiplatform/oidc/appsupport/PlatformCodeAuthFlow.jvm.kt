package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import io.ktor.http.toURI
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.flows.PreferencesCodeAuthFlow
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest
import java.awt.Desktop
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@ExperimentalOpenIdConnect
actual class PlatformCodeAuthFlow internal constructor(
    client: OpenIdConnectClient,
    private val webFlow: WebAuthenticationFlow,
    preferences: Preferences
) : PreferencesCodeAuthFlow(client, preferences), EndSessionFlow {

    actual override suspend fun startLoginFlow(request: AuthCodeRequest) {
        val redirectUrl = request.url.parameters.get("redirect_uri").orEmpty()
        checkRedirectPort(Url(redirectUrl))
        val result = webFlow.startWebFlow(request.url, redirectUrl)
        throwAuthenticationIfCancelled(result)
    }

    actual override suspend fun startLogoutFlow(request: EndSessionRequest) {
        val redirectUrl = request.url.parameters.get("post_logout_redirect_uri").orEmpty()
        checkRedirectPort(Url(redirectUrl))
        val result = webFlow.startWebFlow(request.url, redirectUrl)
        throwEndsessionIfCancelled(result)
    }

    @OptIn(ExperimentalContracts::class)
    private fun checkRedirectPort(redirectUrl: Url?) {
        contract {
            returns() implies (redirectUrl != null)
        }
        if (redirectUrl?.isLocalhost() == false) {
            throw OpenIdConnectException.AuthenticationFailure("JVM implementation can only handle redirect uris using localhost! Redirect uri was: $redirectUrl")
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