package org.publicvalue.multiplatform.oidc.appsupport.webserver

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

@ExperimentalOpenIdConnect
public interface Webserver {
    /**
     * Start a local Webserver on the given port, waiting for the redirectPath to be called.
     *
     * @return Url the redirect was called with, including query parameters.
     */
    public suspend fun startAndWaitForRedirect(redirectPath: String): Url

    /**
     * Stop the webserver.
     */
    public suspend fun stop()
}
