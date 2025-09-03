package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.appsupport.webserver.SimpleKtorWebserver
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.flows.EndSessionFlow

@Suppress("unused")
@ExperimentalOpenIdConnect
class JvmCodeAuthFlowFactory(
    private val webserverProvider: () -> Webserver = { SimpleKtorWebserver() },
    private val openUrl: (Url) -> Unit = { it.openInBrowser() },
): CodeAuthFlowFactory {
    override fun createAuthFlow(client: OpenIdConnectClient): PlatformCodeAuthFlow {
        return PlatformCodeAuthFlow(
            client = client,
            webFlow = WebServerFlow(
                webserverProvider = webserverProvider,
                openUrl = openUrl,
            )
        )
    }

    override fun createEndSessionFlow(client: OpenIdConnectClient): EndSessionFlow {
        return createAuthFlow(client)
    }
}