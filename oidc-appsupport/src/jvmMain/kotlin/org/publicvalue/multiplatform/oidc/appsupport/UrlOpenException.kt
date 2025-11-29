package org.publicvalue.multiplatform.oidc.appsupport

data class UrlOpenException(
    override val message: String?, override val cause: Throwable? = null
): Exception(message, cause)