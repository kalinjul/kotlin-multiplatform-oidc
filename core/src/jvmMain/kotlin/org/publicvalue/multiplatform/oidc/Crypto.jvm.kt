package org.publicvalue.multiplatform.oidc

import java.security.MessageDigest

actual fun String.s256(): ByteArray {
    val sha256 = MessageDigest.getInstance("SHA256")
    return sha256.digest(this.toByteArray(charset = Charsets.US_ASCII))
}