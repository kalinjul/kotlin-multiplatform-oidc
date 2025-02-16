package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.respondText
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

    override suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): ApplicationRequest? {
        server?.stop()
        return suspendCoroutine<ApplicationRequest> {
            embeddedServer(CIO, port = port) {
                routing {
                    get(redirectPath) {
                        createResponse()

                        server?.stop()
                        it.resume(this.call.request)
                    }
                }
            }.apply {
                server = engine
                start(wait = true)
            }
        }
    }

    override suspend fun stop() {
        server?.stop()
    }
}
