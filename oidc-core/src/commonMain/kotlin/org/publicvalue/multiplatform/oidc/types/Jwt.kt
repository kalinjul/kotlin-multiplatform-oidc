package org.publicvalue.multiplatform.oidc.types

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.wrapExceptions
import kotlin.experimental.ExperimentalObjCName
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmInline
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCName

@OptIn(ExperimentalSerializationApi::class)
private val json by lazy {
    Json {
        explicitNulls = false
        ignoreUnknownKeys = true
    }
}

@OptIn(ExperimentalObjCName::class)
@ObjCName("Jwt", "Jwt", exact = true)
data class Jwt(
    val header: JwtHeader,
    val payload: IdToken,
    val signature: String?
) {
    companion object {
        private val base64 by lazy { Base64.withPadding(Base64.PaddingOption.ABSENT) }

        /**
         * JWTs are either encoded using JWS Compact Serialization (signed, 3 parts) or JWE Compact Serialization (encrypted, 5 parts).
         * We only support JWS Compact Serialization.
         */
        @OptIn(ExperimentalObjCRefinement::class)
        @Throws(OpenIdConnectException::class)
        @HiddenFromObjC
        fun parse(string: String): Jwt {
            val parts = string.split('.')
            if (parts.size > 3) {
                throw OpenIdConnectException.UnsupportedFormat("Expected at most 3 JWT token parts (this may be an encrypted token which is unsupported)")
            } else if (parts.size < 2) {
                throw OpenIdConnectException.UnsupportedFormat("Expected at least 2 JWT token parts")
            }

            val headerB64 = parts[0]
            val payloadB64 = parts[1]
            val signatureB64 = parts.getOrNull(2)

            return wrapExceptions {
                Jwt(
                    header = JwtHeader.parse(base64.decode(headerB64).decodeToString()),
                    payload = JwtClaims.parse(base64.decode(payloadB64).decodeToString()).toOpenIdConnectToken(),
                    signature = signatureB64
                )
            }
        }
    }
}

/**
 * https://datatracker.ietf.org/doc/html/rfc7515#section-4.1
 */
@OptIn(ExperimentalObjCName::class)
@Serializable
@ObjCName("JwtHeader", "JwtHeader", exact = true)
data class JwtHeader(
    /** Required: Algorithm. Possible values: https://datatracker.ietf.org/doc/html/rfc7518#section-3.1 **/
    val alg: String,
    val jku: String?,
    val jwk: String?,
    val kid: String?,
    val x5u: String?,
    val x5c: String?,
    val x5t: String?,
    @SerialName("x5t#S256")
    val x5tS256: String?,
    val typ: String?,
    val cty: String?,
    val crit: String?
) {
    companion object {
        fun parse(string: String): JwtHeader {
            return json.decodeFromString<JwtHeader>(string)
        }
    }
}

@OptIn(ExperimentalObjCName::class)
@JvmInline
@ObjCName("JwtClaims", "JwtClaims", exact = true)
value class JwtClaims(
    val claims: Map<String, Any?>
) {
    companion object {
        @Throws(OpenIdConnectException::class)
        fun parse(string: String): JwtClaims {
            try {
                val map = json.decodeFromString<Map<String, JsonElement>>(string)
                    .map { entry ->
                        val value = entry.value
                        entry.key to value.toKotlin(entry.key)
                    }
                    .toMap()
                return JwtClaims(map)
            } catch (e: Exception) {
                throw OpenIdConnectException.TechnicalFailure(e.message ?: "Could not parse JWT", e)
            }
        }

        private fun JsonElement.toKotlin(
            key: String
        ): Any? = when(this) {
            is JsonArray -> this.mapIndexed { index, it ->
                it.toKotlin("$key-$index")
            }

            is JsonObject -> this.map { (key, value) ->
                key to value.toKotlin(key)
            }.toMap()
            is JsonPrimitive -> {
                if (this.isString) {
                    this.content
                } else {
                    this.content.toLongOrNull()
                        ?: this.content.toDoubleOrNull()
                        ?: this.content.toBooleanStrictOrNull()
                        ?: this.content
                }
            }

            is JsonNull -> null
        }
    }
}

private fun JwtClaims.toOpenIdConnectToken(): IdToken =
    IdToken(
        iss = claims["iss"] as String?,
        sub = claims["sub"] as String?,
        aud = claims["aud"]?.parseListOrString(),
        exp = claims["exp"] as Long?,
        iat = claims["iat"] as Long?,
        auth_time = claims["auth_time"] as Long?,
        nonce = claims["nonce"] as String?,
        acr = claims["acr"] as String?,
        amr = claims["amr"]?.parseListOrString(),
        azp = claims["azp"] as String?,
        alg = claims["alg"] as String?,
        kid = claims["kid"] as String?,
        at_hash = claims["at_hash"] as String?,
        additionalClaims = claims
    )

@Suppress("unchecked_cast")
private fun Any.parseListOrString() =
    if (this is List<*>) {
        this as List<String>
    } else {
        listOf(this as String)
    }

// this is visible from swift as JwtKt.parse()
@Throws(OpenIdConnectException::class)
fun String.parseJwt() = Jwt.parse(this)
