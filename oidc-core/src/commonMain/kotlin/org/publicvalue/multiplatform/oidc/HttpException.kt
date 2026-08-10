package org.publicvalue.multiplatform.oidc

import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.decodeURLQueryComponent
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse

@Suppress("unused")
class HttpException(
    override val message: String,
    val statusCode: HttpStatusCode,
    val body: String?,
    val errorResponse: ErrorResponse?,
): Exception(message)

suspend fun HttpResponse.toHttpException(): HttpException {
    val errorResponse = call.errorBody()
    val body = call.body<String>().decodeURLQueryComponent(plusIsSpace = true)
    return HttpException(
        message = "HTTP ${status.value} ${HttpStatusCode.fromValue(status.value).description}",
        statusCode = status,
        body = body,
        errorResponse = errorResponse,
    )
}

private suspend fun HttpClientCall.errorBody(): ErrorResponse? {
    return try {
        body<ErrorResponse>()
    } catch (e: Exception) {
        null
    }
}