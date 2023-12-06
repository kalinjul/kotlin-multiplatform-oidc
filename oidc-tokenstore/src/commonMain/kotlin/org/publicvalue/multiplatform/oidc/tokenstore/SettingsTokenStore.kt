package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect

enum class SettingsKey {
    ACCESSTOKEN, REFRESHTOKEN, IDTOKEN
}

@ExperimentalOpenIdConnect
open class SettingsTokenStore(
    private val settings: SettingsStore
): TokenStore {

    override suspend fun getAccessToken(): String? {
        return runOrNull {
            settings.get(SettingsKey.ACCESSTOKEN.name)
        }
    }

    override suspend fun getRefreshToken(): String? {
        return runOrNull {
            settings.get(SettingsKey.REFRESHTOKEN.name)
        }
    }

    override suspend fun getIdToken(): String? {
        return runOrNull {
            settings.get(SettingsKey.IDTOKEN.name)
        }
    }

    override suspend fun removeAccessToken() {
        runOrNull {
            settings.remove(SettingsKey.ACCESSTOKEN.name)
        }
    }

    override suspend fun removeRefreshToken() {
        runOrNull {
            settings.remove(SettingsKey.REFRESHTOKEN.name)
        }
    }

    override suspend fun removeIdToken() {
        runOrNull {
            settings.remove(SettingsKey.IDTOKEN.name)
        }
    }

    override suspend fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) {
        runOrNull {
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

inline fun <T> runOrNull(block: () -> T?): T? = try {
    block()
} catch (t: Throwable) {
    null
}