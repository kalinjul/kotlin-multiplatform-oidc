package org.publicvalue.multiplatform.oidc.preferences

import io.ktor.http.Url
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

object PreferencesStatic {
    var authRequest: AuthCodeRequest? = null
    var responseUri: Url? = null


}

val PREFERENCES_FILENAME = "oidcsession.preferences_pb"

private enum class PreferenceKeys(val key: String) {
    LAST_REQUEST("lastRequest"),
    RESPONSE_URI("responseUri")
}

suspend fun Preferences.setAuthRequest(request: AuthCodeRequest) {
//    PreferencesStatic.authRequest = request
    put(PreferenceKeys.LAST_REQUEST.key, Json.encodeToString(request))
}

suspend fun Preferences.getAuthRequest(): AuthCodeRequest? {
//    return PreferencesStatic.authRequest
    return get(PreferenceKeys.LAST_REQUEST.key)?.let { Json.decodeFromStringOrNull<AuthCodeRequest>(it) }
}

suspend fun Preferences.setResponseUri(response: Url) {
//    PreferencesStatic.responseUri = response
    put(PreferenceKeys.RESPONSE_URI.key, Json.encodeToString(response))
}

suspend fun Preferences.getResponseUri(): Url? {
//    return PreferencesStatic.responseUri
    return get(PreferenceKeys.RESPONSE_URI.key)?.let { Json.decodeFromStringOrNull<Url>(it) }
}

suspend fun Preferences.clearOidcPreferences() {
    remove(PreferenceKeys.RESPONSE_URI.key)
    remove(PreferenceKeys.LAST_REQUEST.key)
}

inline fun <reified T> Json.decodeFromStringOrNull(string: String): T? {
    try {
        return Json.decodeFromString<T>(string)
    } catch (e: Exception) {
        when (e) {
            is SerializationException, is IllegalArgumentException -> {
                return null
            }
            else -> throw e
        }

    }
}

