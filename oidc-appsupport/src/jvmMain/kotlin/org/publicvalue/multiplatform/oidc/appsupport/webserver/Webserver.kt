package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.server.request.ApplicationRequest
import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult

interface Webserver {
    /**
     * Start a local Webserver on the given port, waiting for the redirectPath to be called.
     *
     * @return RedirectResponse containing authCode + state.
     */
    suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): ApplicationRequest?

    /**
     * Stop the webserver.
     */
    suspend fun stop()
}
