package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

/**
 * A [TokenStore] implementation for JVM desktop that persists tokens
 * in the system's native keyring using java-keyring.
 *
 * Uses [JvmKeyringSettingsStore] backed by:
 * - macOS Keychain (via Security framework / JNA)
 * - Windows Credential Manager (via WinCred API)
 * - Linux Secret Service / KWallet (via D-Bus)
 *
 * Inherits concurrency-safe token management and flow support
 * from [SettingsTokenStore].
 *
 * Usage:
 * ```
 * val tokenStore = JvmKeyringTokenStore(
 *     serviceName = "com.mycompany.myapp"
 * )
 * ```
 *
 * @param serviceName A reverse-DNS style identifier for your app, used to namespace
 *   keyring entries. Should be unique to your application.
 * @param accountPrefix A prefix for individual token entries within the service namespace.
 */
@ExperimentalOpenIdConnect
@Suppress("unused")
class JvmKeyringTokenStore(
    serviceName: String = "org.publicvalue.multiplatform.oidc",
    accountPrefix: String = "tokens"
) : SettingsTokenStore(
    settings = JvmKeyringSettingsStore(
        serviceName = serviceName,
        accountPrefix = accountPrefix
    )
)
