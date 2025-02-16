package org.publicvalue.multiplatform.oidc.sample.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.publicvalue.multiplatform.oidc.sample.domain.ClientSettings
import org.publicvalue.multiplatform.oidc.sample.domain.IdpSettings
import org.publicvalue.multiplatform.oidc.sample.domain.TokenData
import org.publicvalue.multiplatform.oidc.settings.SettingsStore

private val IDP_SETTINGS_KEY = "idp_settings_key"
private val CLIENT_SETTINGS_KEY = "client_settings_key"
private val TOKEN_DATA_KEY = "token_data_key"

class OidcSettingsStore(
    private val settingsStore: SettingsStore
) {
    private val idpSettings = MutableStateFlow<IdpSettings?>(null)
    private val clientSettings = MutableStateFlow<ClientSettings?>(null)
    private val tokenData = MutableStateFlow<TokenData?>(null)

    fun observeIdpSettings() = idpSettings.asStateFlow()
    fun observeClientSettings() = clientSettings.asStateFlow()
    fun observeTokenData() = tokenData.asStateFlow()

    private val scope by lazy { CoroutineScope(Dispatchers.Default + SupervisorJob()) }
    
    init {
        scope.launch {
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

    suspend fun setTokenData(tokenData: TokenData) {
        settingsStore.put(TOKEN_DATA_KEY, Json.encodeToString(tokenData))
        this.tokenData.value = tokenData
    }

    suspend fun clearTokenData() {
        settingsStore.remove(TOKEN_DATA_KEY)
        this.tokenData.value = null
    }
}