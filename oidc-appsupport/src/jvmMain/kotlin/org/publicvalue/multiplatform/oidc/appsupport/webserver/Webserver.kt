package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@ExperimentalOpenIdConnect
interface Webserver {
    /**
     * Start a local Webserver on the given port, waiting for the redirectPath to be called.
     *
     * @return Url the redirect was called with, including query parameters.
     */
    suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): Url

    /**
     * Start the webserver on the given port and wait until it's ready to accept connections.
     * This method returns once the server is fully started and ready to handle requests.
     *
     * @param port The port to start the server on
     * @param redirectPath The path to handle redirects on
     */
    suspend fun start(port: Int, redirectPath: String)

    /**
     * Wait for a redirect request to be received on the previously configured redirect path.
     * This method should be called after start() and will block until a redirect is received.
     *
     * @return Url the redirect was called with, including query parameters.
     */
    suspend fun waitForRedirect(): Url

    /**
     * Stop the webserver.
     */
    suspend fun stop()
}
