package org.publicvalue.multiplatform.oidc.tokenstore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

enum class SettingsKey {
    ACCESSTOKEN, REFRESHTOKEN, IDTOKEN
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

    private val currentAccessToken = MutableStateFlow<String?>(null)
    private val currentRefreshToken = MutableStateFlow<String?>(null)
    private val currentIdToken = MutableStateFlow<String?>(null)

    private var accessTokenLoaded = false
    private var refreshTokenLoaded = false
    private var idTokenLoaded = false

    override val accessTokenFlow get() = flow {
        if (!accessTokenLoaded) {
            accessTokenLoaded = true
            currentAccessToken.value = getAccessToken()
        }
        emitAll(currentAccessToken)
    }

    override val refreshTokenFlow get() = flow {
        if (!refreshTokenLoaded) {
            refreshTokenLoaded = true
            currentRefreshToken.value = getRefreshToken()
        }
        emitAll(currentRefreshToken)
    }

    override val idTokenFlow get() = flow {
        if (!idTokenLoaded) {
            idTokenLoaded = true
            currentIdToken.value = getIdToken()
        }
        emitAll(currentIdToken)
    }

    override suspend fun getAccessToken(): String? {
        return runOrNull {
            mutex.withLock {
                settings.get(SettingsKey.ACCESSTOKEN.name)
            }
        }
    }

    override suspend fun getRefreshToken(): String? {
        return runOrNull {
            mutex.withLock {
                settings.get(SettingsKey.REFRESHTOKEN.name)
            }
        }
    }

    override suspend fun getIdToken(): String? {
        return runOrNull {
            mutex.withLock {
                settings.get(SettingsKey.IDTOKEN.name)
            }
        }
    }

    override suspend fun removeAccessToken() {
        runOrNull {
            mutex.withLock {
                settings.remove(SettingsKey.ACCESSTOKEN.name)
                currentAccessToken.value = null
            }
        }
    }

    override suspend fun removeRefreshToken() {
        runOrNull {
            mutex.withLock {
                settings.remove(SettingsKey.REFRESHTOKEN.name)
                currentRefreshToken.value = null
            }
        }
    }

    override suspend fun removeIdToken() {
        runOrNull {
            mutex.withLock {
                settings.remove(SettingsKey.IDTOKEN.name)
                currentIdToken.value = null
            }
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) {
        runOrNull {
            mutex.withLock {
                settings.put(SettingsKey.ACCESSTOKEN.name, accessToken)
                if (refreshToken != null) {
                    settings.put(SettingsKey.REFRESHTOKEN.name, refreshToken)
                } else {
                    settings.remove(SettingsKey.REFRESHTOKEN.name)
                }
                if (idToken != null) {
                    settings.put(SettingsKey.IDTOKEN.name, idToken)
                } else {
                    settings.remove(SettingsKey.IDTOKEN.name)
                }
                // update cached values
                currentAccessToken.value = accessToken
                currentRefreshToken.value = refreshToken
                currentIdToken.value = idToken
            }
        }
    }
}

// catch anything to avoid crashes on ios
inline fun <T> runOrNull(block: () -> T?): T? = try {
    block()
} catch (t: Throwable) {
    println(t.message)
    null
}