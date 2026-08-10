package org.publicvalue.multiplatform.oidc.util

import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse

suspend fun HttpClientCall.errorBody(): ErrorResponse? {
    return try {
        body<ErrorResponse>()
    } catch (e: Exception) {
        null
    }
}