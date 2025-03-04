package org.publicvalue.multiplatform.oidc

import io.ktor.util.encodeBase64
import kotlin.random.Random

/**
 * Generate random bytes using a cryptographically secure random
 * number generator.
 *
 * On Android, this delegates to [`java.security.SecureRandom`](https://developer.android.com/reference/java/security/SecureRandom)
 *
 * On iOS, this delegates to [`SecRandomCopyBytes`](https://developer.apple.com/documentation/security/1399291-secrandomcopybytes?language=objc)
 *
 * On wasmJs, this delegates to [`crypto.getRandomValues`](https://developer.mozilla.org/en-US/docs/Web/API/Crypto/getRandomValues)
 *
 * @param size number of bytes to generate
 */
expect fun secureRandomBytes(size: Int = 32): ByteArray

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