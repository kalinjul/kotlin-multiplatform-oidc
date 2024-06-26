package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.*
import io.ktor.server.routing.*

class Webserver() {
    private var server: CIOApplicationEngine? = null

    suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): ApplicationRequest? {
        var call: ApplicationRequest? = null
        server?.stop()
        embeddedServer(CIO, port = port) {
            routing {
                get("/redirect") {
                    this.call.respond(status = HttpStatusCode.OK, Unit)
                    call = this.call.request
                    server?.stop()
                }
            }
        }.apply {
            server = engine
            start(wait = true)
        }
        return call
    }

    fun stop() {
        server?.stop()
    }
}