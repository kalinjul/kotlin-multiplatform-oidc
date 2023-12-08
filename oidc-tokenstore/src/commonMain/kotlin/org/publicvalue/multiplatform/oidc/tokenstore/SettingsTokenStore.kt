package org.publicvalue.multiplatform.oidc.tokenstore

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

enum class SettingsKey {
    ACCESSTOKEN, REFRESHTOKEN, IDTOKEN
}

@ExperimentalOpenIdConnect
open class SettingsTokenStore(
    private val settings: SettingsStore
): TokenStore() {

    private val mutex = Mutex(false)

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
            }
        }
    }

    override suspend fun removeRefreshToken() {
        runOrNull {
            mutex.withLock {
                settings.remove(SettingsKey.REFRESHTOKEN.name)
            }
        }
    }

    override suspend fun removeIdToken() {
        runOrNull {
            mutex.withLock {
                settings.remove(SettingsKey.IDTOKEN.name)
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
            }
        }
    }
}

inline fun <T> runOrNull(block: () -> T?): T? = try {
    block()
} catch (t: Throwable) {
    null
}