package org.publicvalue.multiplatform.oidc

import io.ktor.util.encodeBase64
import kotlin.random.Random

fun randomBytes(size: Int = 32): ByteArray {
    val random = Random.Default
    val bytes = random.nextBytes(size)
    return bytes
}

/**
 * base64urlencode
 * See https://datatracker.ietf.org/doc/html/rfc7636#appendix-A
 */
fun ByteArray.encodeForPKCE() = this.encodeBase64()
    .replace("=", "")
    .replace("+", "-")
    .replace("/", "_")

expect fun String.s256(): ByteArray