package org.publicvalue.multiplatform.oidc.tokenstore

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

/**
 * A [SettingsStore] implementation for JVM desktop that uses the system's native keyring
 * via the java-keyring library.
 *
 * Security guarantees vary by platform and packaging:
 * - macOS: Strong isolation when distributed via Mac App Store (signed bundle, sandboxed)
 * - Windows: Strong isolation when distributed via Microsoft Store (MSIX package identity)
 * - Linux: Encrypted at rest, but no mandatory access control once keyring is unlocked.
 *   When distributed via Snap, declare the password-manager-service interface in snapcraft.yaml.
 *
 * @param serviceName The service name used to namespace entries in the keyring.
 * @param accountPrefix The prefix used to distinguish token entries from other keyring entries.
 */
@ExperimentalOpenIdConnect
class JvmKeyringSettingsStore(
    private val serviceName: String = "org.publicvalue.multiplatform.oidc",
    private val accountPrefix: String = "tokens"
) : SettingsStore {

    private val mutex = Mutex()

    private val keyring: Keyring by lazy { Keyring.create() }
    private val writtenKeys = mutableSetOf<String>()

    private fun accountKey(key: String) = "$accountPrefix:$key"

    override suspend fun get(key: String): String? {
        return try {
            keyring.getPassword(serviceName, accountKey(key))
        } catch (e: PasswordAccessException) {
            // Key not found - this is expected when tokens haven't been stored yet
            null
        } catch (e: Exception) {
            println("JvmKeyringSettingsStore: failed to read key '$key': ${e.message}")
            null
        }
    }

    override suspend fun put(key: String, value: String) {
        try {
            keyring.setPassword(serviceName, accountKey(key), value)
            mutex.withLock {
                writtenKeys.add(key)
            }
        } catch (e: Exception) {
            // Keyring may be unavailable (e.g. no Secret Service daemon on Linux,
            // or keychain locked on macOS). Log and rethrow so callers are aware.
            println("JvmKeyringSettingsStore: failed to write key '$key': ${e.message}")
            throw e
        }
    }

    override suspend fun remove(key: String) {
        try {
            keyring.deletePassword(serviceName, accountKey(key))
            mutex.withLock {
                writtenKeys.remove(key)
            }
        } catch (e: PasswordAccessException) {
            mutex.withLock {
                writtenKeys.remove(key)
            }
        } catch (e: Exception) {
            println("JvmKeyringSettingsStore: failed to remove key '$key': ${e.message}")
        }
    }

    override suspend fun clear() {
        val keysToRemove = mutex.withLock {
            val keys = writtenKeys.toSet()
            writtenKeys.clear()
            keys
        }

        keysToRemove.forEach { remove(it) }

        // Also explicitly attempt removal of known SettingsKey entries
        // in case they were written before this instance was created (e.g. on a previous app run)
        SettingsKey.entries.forEach { settingsKey ->
            runCatching {
                keyring.deletePassword(serviceName, accountKey(settingsKey.name))
            }.onFailure { e ->
                if (e !is PasswordAccessException) {
                    println("JvmKeyringSettingsStore: failed to clear default key '${settingsKey.name}': ${e.message}")
                }
            }
        }
    }
}