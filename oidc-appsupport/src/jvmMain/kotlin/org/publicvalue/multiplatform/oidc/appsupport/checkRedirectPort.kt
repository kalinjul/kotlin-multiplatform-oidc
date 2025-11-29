package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal fun checkRedirectPort(redirectUrl: Url?) {
    contract {
        returns() implies (redirectUrl != null)
    }
    if (redirectUrl?.isLocalhost() == false) {
        throw OpenIdConnectException.AuthenticationFailure("JVM implementation can only handle redirect uris using localhost! Redirect uri was: $redirectUrl")
    }
}
private fun Url.isLocalhost(): Boolean {
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
