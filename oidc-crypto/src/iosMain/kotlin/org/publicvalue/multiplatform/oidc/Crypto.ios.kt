package org.publicvalue.multiplatform.oidc

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import org.publicvalue.multiplatform.oidc.util.KCrypto
import platform.Foundation.NSData
import platform.Foundation.create
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)

    bytes.usePinned { pin ->
        val ptr = pin.addressOf(0)
        val status = SecRandomCopyBytes(kSecRandomDefault, bytes.size.convert(), ptr)
        if (status != errSecSuccess) {
            error("Unable to fill random bytes. errorCode=$status")
        }
    }

    return bytes
}

@OptIn(ExperimentalForeignApi::class)
actual fun String.s256(): ByteArray {
    val result = KCrypto.sha256(this)
    return result.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned {
            memcpy(it.addressOf(0), bytes, length)
        }
    }
}

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
fun ByteArray.toNSData(): NSData = memScoped {
    NSData.create(bytes = allocArrayOf(this@toNSData), length = this@toNSData.size.toULong())
}