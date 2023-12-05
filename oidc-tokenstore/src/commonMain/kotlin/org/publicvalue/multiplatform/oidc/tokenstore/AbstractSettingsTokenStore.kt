package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

enum class SettingsKey {
    ACCESSTOKEN, REFRESHTOKEN, IDTOKEN
}

open class AbstractSettingsTokenStore(
    val settings: SettingsStore
): TokenStore() {

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

    override suspend fun saveTokens(tokens: AccessTokenResponse, removeIfNull: Boolean) {
        runOrNull {
            settings.put(SettingsKey.ACCESSTOKEN.name, tokens.access_token)
            tokens.refresh_token?.let { settings.put(SettingsKey.REFRESHTOKEN.name, it) }
            tokens.id_token?.let { settings.put(SettingsKey.IDTOKEN.name, it) }

            if (removeIfNull) {
                if (tokens.refresh_token == null) settings.remove(SettingsKey.REFRESHTOKEN.name)
                if (tokens.id_token == null) settings.remove(SettingsKey.IDTOKEN.name)
            }
        }
    }
}

inline fun <T> runOrNull(block: () -> T?): T? = try {
    block()
} catch (t: Throwable) {
    null
}