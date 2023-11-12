package org.publicvalue.multiplatform.oidc

import io.ktor.client.HttpClient
import io.ktor.http.URLParserException
import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.discovery.OpenIDConnectConfiguration

class OpenIDConnectClient(
    val httpClient: HttpClient = HttpClient()
) {

}

fun OpenIDConnectConfiguration.getTokenRequestUrl(): Url =
    if (token_endpoint != null) {
        try {
            Url(token_endpoint)
        } catch (e: URLParserException) {
            throw OpenIDConnectException.InvalidUrl(token_endpoint, e)
        }
    } else {
        throw OpenIDConnectException.InvalidUrl(token_endpoint)
    }