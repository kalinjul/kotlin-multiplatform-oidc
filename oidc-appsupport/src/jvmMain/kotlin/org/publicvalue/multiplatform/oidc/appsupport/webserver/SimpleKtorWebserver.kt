package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.request.uri
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeout
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult

@ExperimentalOpenIdConnect
class SimpleKtorWebserver(
    val createResponse: suspend RoutingContext.() -> Unit = {
        call.respondText(
            status = HttpStatusCode.OK,
            text = """
                <!DOCTYPE html>
                <html lang="en">
                  <head>
                    <meta charset="utf-8">
                    <title>Authorization redirect successful</title>
                  </head>
                  <body>
                    <h1>You may now close this page and return to your app.</h1>
                  </body>
                </html>
            """.trimIndent(),
            contentType = ContentType.parse("text/html")
        )
    }
): Webserver {
    private var server: CIOApplicationEngine? = null
    private var redirectReceived: CompletableDeferred<ApplicationRequest>? = null

    override suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): Url {
        start(port, redirectPath)
        return waitForRedirect()
    }

    override suspend fun start(port: Int, redirectPath: String) {
        server?.stop()
        
        // Create deferred for redirect capture
        redirectReceived = CompletableDeferred<ApplicationRequest>()
        
        embeddedServer(CIO, port = port) {
            routing {
                get(redirectPath) {
                    createResponse()
                    
                    // Complete the class-level deferred with the request
                    redirectReceived?.complete(this.call.request)
                }
            }
        }.apply {
            server = engine
            start(wait = false)
        }
    }

    override suspend fun waitForRedirect(): Url {
        // Wait for redirect with timeout (5 minutes)
        val call = withTimeout(300_000) {
            redirectReceived!!.await()
        }
        
        // Cleanup
        server?.stop()
        
        return Url(call.uri)
    }

    override suspend fun stop() {
        server?.stop()
    }
}
