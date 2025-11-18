package org.publicvalue.multiplatform.oidc.preferences

import io.ktor.http.Url
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.EndSessionRequest

val PREFERENCES_FILENAME = "org.publicvalue.multiplatform.oidc.oidcsession"

private enum class PreferenceKeys(val key: String) {
    LAST_AUTH_REQUEST("lastAuthRequest"),
    LAST_ENDSESSION_REQUEST("lastEndsessionRequest"),
    RESPONSE_URI("responseUri")
}

suspend fun Preferences.setAuthRequest(request: AuthCodeRequest) {
    put(PreferenceKeys.LAST_AUTH_REQUEST.key, Json.encodeToString(request))
}

suspend fun Preferences.getAuthRequest(): AuthCodeRequest? {
    return get(PreferenceKeys.LAST_AUTH_REQUEST.key)?.let { Json.decodeFromStringOrNull<AuthCodeRequest>(it) }
}

suspend fun Preferences.setEndSessionRequest(request: EndSessionRequest) {
    put(PreferenceKeys.LAST_ENDSESSION_REQUEST.key, Json.encodeToString(request))
}

suspend fun Preferences.getEndsessionRequest(): EndSessionRequest? {
    return get(PreferenceKeys.LAST_ENDSESSION_REQUEST.key)?.let { Json.decodeFromStringOrNull<EndSessionRequest>(it) }
}

suspend fun Preferences.setResponseUri(response: Url) {
    put(PreferenceKeys.RESPONSE_URI.key, Json.encodeToString(response))
}

suspend fun Preferences.getResponseUri(): Url? {
    return get(PreferenceKeys.RESPONSE_URI.key)?.let { Json.decodeFromStringOrNull<Url>(it) }
}

suspend fun Preferences.clearOidcPreferences() {
    remove(PreferenceKeys.RESPONSE_URI.key)
    remove(PreferenceKeys.LAST_AUTH_REQUEST.key)
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

