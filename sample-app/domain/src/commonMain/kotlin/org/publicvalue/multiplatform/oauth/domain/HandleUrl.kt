package org.publicvalue.multiplatform.oauth.domain

import io.ktor.http.Url

expect class HandleUrl {
    operator fun invoke(uri: Url)
}

data class UrlOpenException(
    override val message: String?, override val cause: Throwable? = null
): Exception(message, cause)