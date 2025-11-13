package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow
import org.publicvalue.multiplatform.oidc.preferences.PREFERENCES_FILENAME
import org.publicvalue.multiplatform.oidc.preferences.PreferencesFactory

@Suppress("unused")
@ExperimentalOpenIdConnect
class JvmCodeAuthFlowFactory(
    private val webserverProvider: () -> Webserver = { SimpleKtorWebserver() },
    private val openUrl: (Url) -> Unit = { it.openInBrowser() },
    /** factory used to create preferences to save session information during login process. **/
    private val preferencesFactory: PreferencesFactory = PreferencesFactory()
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        val preferences = preferencesFactory.create(PREFERENCES_FILENAME)
        return PlatformCodeAuthFlow(
            client = client,
            webFlow = WebServerFlow(
                webserverProvider = webserverProvider,
                openUrl = openUrl,
                preferences = preferences
            ),
            preferences = preferences
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}