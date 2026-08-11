package org.publicvalue.multiplatform.oidc.tokenstore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

enum class SettingsKey {
    /** Legacy keys */
    ACCESSTOKEN, REFRESHTOKEN, IDTOKEN,

    /** Current key, holds the whole [AccessTokenResponse] as JSON. */
    TOKENS
}

/**
 * Android Implementation: [org.publicvalue.multiplatform.oidc.tokenstore.AndroidSettingsTokenStore]
 * iOS implementation: [KeychainTokenStore]
 */
@ExperimentalOpenIdConnect
open class SettingsTokenStore(
    private val settings: SettingsStore
): TokenStore() {

    private val mutex = Mutex(false)

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val currentTokens = MutableStateFlow<AccessTokenResponse?>(null)

    private var tokensLoaded = false
    private var legacyMigrated = false

    override val tokenResponseFlow: Flow<AccessTokenResponse?> get() = flow {
        if (!tokensLoaded) {
            getTokenResponse()
        }
        emitAll(currentTokens)
    }

    @Deprecated("Use tokenResponseFlow instead")
    override val accessTokenFlow get() = tokenResponseFlow.map { it?.access_token }

    @Deprecated("Use tokenResponseFlow instead")
    override val refreshTokenFlow get() = tokenResponseFlow.map { it?.refresh_token }

    @Deprecated("Use tokenResponseFlow instead")
    override val idTokenFlow get() = tokenResponseFlow.map { it?.id_token }

    override suspend fun getTokenResponse(): AccessTokenResponse? {
        return runOrNull {
            mutex.withLock {
                readTokens()
            }
        }
    }

    override suspend fun getAccessToken(): String? = getTokenResponse()?.access_token

    override suspend fun getRefreshToken(): String? = getTokenResponse()?.refresh_token

    override suspend fun getIdToken(): String? = getTokenResponse()?.id_token

    override suspend fun removeTokens() {
        runOrNull {
            mutex.withLock {
                writeTokens(null)
            }
        }
    }

    override suspend fun saveTokens(tokens: AccessTokenResponse) {
        runOrNull {
            mutex.withLock {
                writeTokens(tokens)
            }
        }
    }

    /** Must be called while holding [mutex]. */
    private suspend fun readTokens(): AccessTokenResponse? {
        val tokens = settings.get(SettingsKey.TOKENS.name)?.let { stored ->
            runOrNull { json.decodeFromString<AccessTokenResponse>(stored) }
        } ?: migrateLegacyTokens()

        tokensLoaded = true
        currentTokens.value = tokens
        return tokens
    }

    /** Must be called while holding [mutex]. */
    private suspend fun writeTokens(tokens: AccessTokenResponse?) {
        if (tokens != null) {
            settings.put(SettingsKey.TOKENS.name, json.encodeToString(tokens))
        } else {
            settings.remove(SettingsKey.TOKENS.name)
        }
        removeLegacyTokens()

        tokensLoaded = true
        currentTokens.value = tokens
    }

    /**
     * Migrates tokens to current json format. Must be called while holding [mutex].
     */
    private suspend fun migrateLegacyTokens(): AccessTokenResponse? {
        if (legacyMigrated) return null

        val accessToken = settings.get(SettingsKey.ACCESSTOKEN.name)
        if (accessToken == null) {
            legacyMigrated = true
            return null
        }

        val tokens = AccessTokenResponse(
            access_token = accessToken,
            refresh_token = settings.get(SettingsKey.REFRESHTOKEN.name),
            id_token = settings.get(SettingsKey.IDTOKEN.name)
        )
        writeTokens(tokens)
        return tokens
    }

    private suspend fun removeLegacyTokens() {
        if (legacyMigrated) return
        settings.remove(SettingsKey.ACCESSTOKEN.name)
        settings.remove(SettingsKey.REFRESHTOKEN.name)
        settings.remove(SettingsKey.IDTOKEN.name)
        legacyMigrated = true
    }
}

// catch anything to avoid crashes on ios
inline fun <T> runOrNull(block: () -> T?): T? = try {
    block()
} catch (t: Throwable) {
    println(t.message)
    null
}
