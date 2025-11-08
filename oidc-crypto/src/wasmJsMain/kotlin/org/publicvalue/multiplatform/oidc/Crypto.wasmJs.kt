package org.publicvalue.multiplatform.oidc

import io.ktor.utils.io.core.toByteArray
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get
import org.kotlincrypto.hash.sha2.SHA256

/**
 * this delegates to [`crypto.getRandomValues`](https://developer.mozilla.org/en-US/docs/Web/API/Crypto/getRandomValues)
 */
@JsName("crypto")
private external object Crypto {
    @Suppress("unused")
    fun getRandomValues(uint8Array: Uint8Array)
}

public actual fun secureRandomBytes(size: Int): ByteArray {
    require(size >= 0) { "count cannot be negative" }
    val uint8Array = Uint8Array(size)
    Crypto.getRandomValues(uint8Array = uint8Array)
    return ByteArray(size) { uint8Array[it] }
}

public actual fun String.s256(): ByteArray {
    val digest = SHA256()
    digest.update(this.toByteArray())
    return digest.digest()
}
