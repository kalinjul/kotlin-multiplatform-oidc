package org.publicvalue.multiplatform.oidc.appsupport;

import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver

@ExperimentalOpenIdConnect
internal class WebServerFlow(
    private val webserverProvider: () -> Webserver,
    private val openUrl: (Url) -> Unit,
) {
    internal suspend fun startWebFlow(requestUrl: Url, redirectUrl: Url, port: Int): Url {
        val webserver = webserverProvider()
        val response = withContext(Dispatchers.IO) {
            async {
                webserver.start(port, redirectPath = redirectUrl.encodedPath)
                openUrl(requestUrl)
                val response = webserver.waitForRedirect()
                webserver.stop()
                response
            }.await()
        }
        return response
    }
}
