package org.publicvalue.multiplatform.oidc

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
public actual fun secureRandomBytes(size: Int): ByteArray {
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
public actual fun String.s256(): ByteArray {
    val inputBytes = this.encodeToByteArray()

    @Suppress("MagicNumber")
    val output = UByteArray(32) // 32 bytes for the SHA-256

    inputBytes.usePinned { inputPin ->
        output.usePinned { outputPin ->
            CC_SHA256(
                inputPin.addressOf(0),
                inputBytes.size.toUInt(),
                outputPin.addressOf(0)
            )
        }
    }

    return ByteArray(output.size) { output[it].toByte() }
}
