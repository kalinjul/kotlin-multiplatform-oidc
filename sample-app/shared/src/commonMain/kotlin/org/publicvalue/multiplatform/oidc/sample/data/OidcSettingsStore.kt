package org.publicvalue.multiplatform.oidc.sample.data

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.settings.SettingsStore
import org.publicvalue.multiplatform.oidc.types.CodeChallengeMethod
import kotlin.coroutines.CoroutineContext

@Serializable
data class IdpSettings(
    val discoveryUrl: String? = null,
    val endpointToken: String? = null,
    val endpointAuthorization: String? = null,
    val endpointDeviceAuthorization: String? = null,
    val endpointEndSession: String? = null,
    val endpointUserInfo: String? = null,
    val endpointIntrospection: String? = null,
) {
    companion object {
        val Empty = IdpSettings()
    }
}

@Serializable
data class ClientSettings(
    val name: String? = null,
    val client_id: String? = null,
    val client_secret: String? = null,
    val scope: String? = null,
    val code_challenge_method: CodeChallengeMethod = CodeChallengeMethod.off,
) {
    companion object {
        val Empty = ClientSettings()
    }
}

class OidcSettingsStore(
    private val settingsStore: SettingsStore
) {
    private val idpSettings = MutableStateFlow<IdpSettings?>(null)
    private val clientSettings = MutableStateFlow<ClientSettings?>(null)

    fun observeIdpSettings() = idpSettings.asStateFlow()
    fun observeClientSettings() = clientSettings.asStateFlow()

    init {
        GlobalScope.launch {
            settingsStore.get(IDP_SETTINGS_KEY)?.let {
                idpSettings.value = Json.decodeFromString(it)
            }
            settingsStore.get(CLIENT_SETTINGS_KEY)?.let {
                clientSettings.value = Json.decodeFromString(it)
            }
        }
    }

    suspend fun setIdpSettings(idpSettings: IdpSettings) {
        settingsStore.put(IDP_SETTINGS_KEY, Json.encodeToString(idpSettings))
        this.idpSettings.value = idpSettings
    }

    suspend fun setClientSettings(clientSettings: ClientSettings) {
        settingsStore.put(CLIENT_SETTINGS_KEY, Json.encodeToString(clientSettings))
        this.clientSettings.value = clientSettings
    }

    private val IDP_SETTINGS_KEY = "idp_settings_key"
    private val CLIENT_SETTINGS_KEY = "client_settings_key"
}