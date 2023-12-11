package org.publicvalue.multiplatform.oidc

import io.ktor.util.encodeBase64
import kotlin.random.Random

/**
 * Generate random bytes using [Random] and return as byte array.
 * @param number of bytes to generate
 */
fun randomBytes(size: Int = 32): ByteArray {
    val random = Random.Default
    val bytes = random.nextBytes(size)
    return bytes
}

/**
 * Implementation of base64urlencode,
 * see [RFC7636](https://datatracker.ietf.org/doc/html/rfc7636#appendix-A)
 */
fun ByteArray.encodeForPKCE() = this.encodeBase64()
    .replace("=", "")
    .replace("+", "-")
    .replace("/", "_")

/**
 * Calculate a SHA-256 hash of the String.
 *
 * @receiver the string to hash
 * @return SHA-256 hash as byte array
 */
expect fun String.s256(): ByteArray