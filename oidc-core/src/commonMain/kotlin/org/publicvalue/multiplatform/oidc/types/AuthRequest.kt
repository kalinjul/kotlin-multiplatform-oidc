package org.publicvalue.multiplatform.oidc.types

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.publicvalue.multiplatform.oidc.OpenIdConnectClientConfig
import org.publicvalue.multiplatform.oidc.flows.Pkce
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

sealed interface AuthRequest {

    val url: Url
    val config: OpenIdConnectClientConfig
    val parameters: Parameters
        get() = url.parameters

    fun validate(value: String): Boolean

    @OptIn(ExperimentalObjCName::class)
    @ObjCName(swiftName = "AuthCodeRequest", name = "AuthCodeRequest", exact = true)
    data class Code(
        override val url: Url,
        override val config: OpenIdConnectClientConfig,
        val pkce: Pkce,
        val state: String,
        val nonce: String
    ) : AuthRequest {
        override fun validate(value: String): Boolean {
            return value == this.state
        }
    }

    @OptIn(ExperimentalObjCName::class)
    @ObjCName(swiftName = "AuthTokenRequest", name = "AuthTokenRequest", exact = true)
    sealed interface Token : AuthRequest {

        override fun validate(value: String): Boolean = true
        suspend fun prepare(
            client: HttpClient,
            configure: (HttpRequestBuilder.() -> Unit)?
        ): HttpStatement

        data class UrlEncoded(
            override val url: Url,
            override val config: OpenIdConnectClientConfig
        ) : Token {
            override suspend fun prepare(
                client: HttpClient,
                configure: (HttpRequestBuilder.() -> Unit)?
            ): HttpStatement = client.prepareGet(url) {
                configure?.invoke(this)
            }
        }

        data class PostBody(
            override val url: Url,
            override val parameters: Parameters,
            override val config: OpenIdConnectClientConfig
        ) : Token {
            override suspend fun prepare(
                client: HttpClient,
                configure: (HttpRequestBuilder.() -> Unit)?
            ): HttpStatement = client.prepareForm(parameters) {
                url(this@PostBody.url)
                configure?.invoke(this)
            }
        }
    }
}