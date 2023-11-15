package org.publicvalue.multiplatform.oidc.discovery

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.http.isSuccess
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.types.OpenIDConnectConfiguration

class Discover(
    val httpClient: HttpClient = HttpClient()
) {

    @OptIn(ExperimentalSerializationApi::class)
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun downloadConfiguration(configurationUrl: String): OpenIDConnectConfiguration {
        return downloadConfiguration(Url(configurationUrl))
    }

    suspend fun downloadConfiguration(configurationUrl: Url): OpenIDConnectConfiguration {
        val result = httpClient.get(configurationUrl)
        val configuration: OpenIDConnectConfiguration = result.forceUnwrapBody(json)
        return configuration
    }
}

suspend inline fun <reified T: Any> HttpResponse.forceUnwrapBody(json: Json = Json): T =
    if (call.response.status.isSuccess()) {
        val bodyString:String = call.body()
        json.decodeFromString(bodyString)
    } else {
        throw Exception("Could not download discovery document: $this")
    }