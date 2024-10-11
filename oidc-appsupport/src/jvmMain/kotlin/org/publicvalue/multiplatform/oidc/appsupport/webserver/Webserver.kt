package org.publicvalue.multiplatform.oidc.appsupport.webserver

import org.publicvalue.multiplatform.oidc.flows.AuthCodeResult

interface Webserver {
    /**
     * Start a local Webserver on the given port, waiting for the redirectPath to be called.
     *
     * @return RedirectResponse containing authCode + state.
     */
    suspend fun startAndWaitForRedirect(port: Int, redirectPath: String): AuthCodeResult

    /**
     * Stop the webserver.
     */
    suspend fun stop()
}
