package org.publicvalue.multiplatform.oidc.appsupport;

import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.preferences.Preferences
import org.publicvalue.multiplatform.oidc.preferences.setResponseUri

@ExperimentalOpenIdConnect
internal class WebServerFlow(
    private val webserverProvider: () -> Webserver,
    private val openUrl: (Url) -> Unit,
    private val preferences: Preferences,
): WebAuthenticationFlow {
    override suspend fun startWebFlow(requestUrl: Url, redirectUrl: String): WebAuthenticationFlowResult {
        val webserver = webserverProvider()
        val response = withContext(Dispatchers.IO) {
            async {
                openUrl(requestUrl)
                val response = webserver.startAndWaitForRedirect(redirectPath = Url(redirectUrl).encodedPath)
                preferences.setResponseUri(response)
                webserver.stop()
                response
            }.await()
        }
        return WebAuthenticationFlowResult.Success(response)
    }
}
