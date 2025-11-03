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
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@ExperimentalOpenIdConnect
public class SimpleKtorWebserver(
    public val port: Int = 8080,
    public val createResponse: suspend RoutingContext.() -> Unit = {
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
) : Webserver {
    private var server: CIOApplicationEngine? = null

    override suspend fun startAndWaitForRedirect(redirectPath: String): Url {
        var call: ApplicationRequest? = null
        server?.stop()
        embeddedServer(CIO, port = port) {
            routing {
                get(redirectPath) {
                    createResponse()
                    call = this.call.request
                    server?.stop()
                }
            }
        }.apply {
            server = engine
            println("Starting webserver at port $port, waiting for call on $redirectPath")
            start(wait = true)
        }
        return Url(call?.uri ?: "")
    }

    override suspend fun stop() {
        println("Stopping webserver")
        server?.stop()
    }
}
