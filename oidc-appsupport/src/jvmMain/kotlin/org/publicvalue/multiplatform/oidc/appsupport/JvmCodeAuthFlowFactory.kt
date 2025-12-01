package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.Url
import io.ktor.http.toURI
import kotlinx.coroutines.runBlocking
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory
import java.awt.Desktop

@Suppress("unused")
@ExperimentalOpenIdConnect
class JvmCodeAuthFlowFactory(
    private val webserverProvider: () -> Webserver = { SimpleKtorWebserver() },
    private val openUrl: (Url) -> Unit = { it.openInBrowser() },
    /** factory used to create preferences to save session information during login process. **/
    private val preferencesFactory: PreferencesFactory = PreferencesFactory()
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        val preferences = runBlocking { preferencesFactory.getOrCreate(PREFERENCES_FILENAME) }
        return PlatformCodeAuthFlow(
            client = client,
            preferences = preferences,
            webFlow = createWebFlow(preferences),
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        val preferences = runBlocking { preferencesFactory.getOrCreate(PREFERENCES_FILENAME) }
        return PlatformEndSessionFlow(
            client = client,
            preferences = preferences,
            webFlow = createWebFlow(preferences),
        )
    }

    private fun createWebFlow(preferences: Preferences): WebServerFlow = WebServerFlow(
        webserverProvider = webserverProvider,
        openUrl = openUrl,
        preferences = preferences
    )
}

private fun Url.openInBrowser() {
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
