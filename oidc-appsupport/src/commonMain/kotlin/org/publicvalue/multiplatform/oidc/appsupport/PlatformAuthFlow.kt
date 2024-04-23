package org.publicvalue.multiplatform.oidc.appsupport

import io.ktor.http.*
import org.publicvalue.multiplatform.oidc.flows.AuthFlow

expect class PlatformAuthFlow: AuthFlow

internal fun String?.getFragmentParameter(param: String): String? {
    val keys = this?.split("&").orEmpty()
    keys.forEach { key ->
        val values = key.split("=")
        if (values[0] == param) {
            return values.getOrNull(1)?.ifBlank { null }
        }
    }
    return null
}

internal fun Url.getFragmentOrQueryParameter(param: String): String? {
    return this.fragment.getFragmentParameter(param) ?: this.parameters[param]?.ifBlank { null }
}