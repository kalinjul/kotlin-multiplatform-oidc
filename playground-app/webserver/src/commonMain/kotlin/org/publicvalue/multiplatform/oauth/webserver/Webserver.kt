package org.publicvalue.multiplatform.oauth.webserver

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.cio.CIO
import io.ktor.server.cio.CIOApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.request.ApplicationRequest
import io.ktor.server.response.*
import io.ktor.server.routing.*
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.inject.ApplicationScope
import org.publicvalue.multiplatform.oauth.logging.Logger

@Inject
@ApplicationScope
class Webserver(
    val logger: Logger
) {
    private var server: CIOApplicationEngine? = null

    fun startAndWaitForRedirect(port: Int): ApplicationRequest? {
        var request: ApplicationRequest? = null
        server?.stop()
        embeddedServer(CIO, port = port) {
            routing {
                get("/redirect") {
                    request = call.request
                    call.respondText(
                        status = HttpStatusCode.OK,
                        text = """Authorization redirect successful""".trimIndent(),
                        contentType = ContentType.parse("text/plain")
                    )
                }
            }
        }.apply {
            server = engine
            logger.d { "Starting Webserver" }
            start(wait = true)
        }
        return request
    }

    fun stop() {
        logger.d { "Stopping Webserver" }
        server?.stop()
    }
}