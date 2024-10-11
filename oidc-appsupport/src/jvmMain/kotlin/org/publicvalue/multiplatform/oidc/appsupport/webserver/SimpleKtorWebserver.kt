package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult

class SimpleKtorWebserver(
    val createResponse: suspend io.ktor.util.pipeline.PipelineContext<Unit, ApplicationCall>.() -> Unit = {
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

    override suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): AuthCodeResult {
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
            server = this
            start(wait = true)
        }
        val code = call?.queryParameters?.get("code")
        val state = call?.queryParameters?.get("code")
        return AuthCodeResult(code, state)
    }

    override suspend fun stop() {
        server?.stop()
    }
}
