package org.publicvalue.multiplatform.oidc.tokenstore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Concurrency-safe Token Store implementations.
 *
 * Android Implementation: [org.publicvalue.multiplatform.oidc.tokenstore.AndroidSettingsTokenStore]
 * iOS implementation: [KeychainTokenStore]
 */
@ExperimentalOpenIdConnect
@OptIn(ExperimentalObjCName::class)
@ObjCName("TokenStoreProtocol", "TokenStoreProtocol", exact = true)
// not an interface to support extension methods in swift
abstract class TokenStore {
    abstract suspend fun getAccessToken(): String?
    abstract suspend fun getRefreshToken(): String?
    abstract suspend fun getIdToken(): String?

    abstract suspend fun getTokenResponse(): AccessTokenResponse?

    @Deprecated("Use tokenResponseFlow instead")
    abstract val accessTokenFlow: Flow<String?>
    @Deprecated("Use tokenResponseFlow instead")
    abstract val refreshTokenFlow: Flow<String?>
    @Deprecated("Use tokenResponseFlow instead")
    abstract val idTokenFlow: Flow<String?>

    abstract val tokenResponseFlow: Flow<AccessTokenResponse?>

    abstract suspend fun removeTokens()

    suspend fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?) {
        saveTokens(
            AccessTokenResponse(
                access_token = accessToken,
                refresh_token = refreshToken,
                id_token = idToken
            )
        )
    }

    abstract suspend fun saveTokens(tokens: AccessTokenResponse)
}

// extension method so no need to overwrite in swift subclasses
@ExperimentalOpenIdConnect
suspend fun TokenStore.getTokens(): OauthTokens? {
    val response = getTokenResponse()

    return if (response != null) {
        OauthTokens(
            accessToken = response.access_token,
            refreshToken = response.refresh_token,
            idToken = response.id_token
        )
    } else {
        null
    }
}

@ExperimentalOpenIdConnect
@Deprecated("Use tokenResponseFlow instead")
val TokenStore.tokensFlow: Flow<OauthTokens?>
    get() = tokenResponseFlow.map { response ->
        if (response != null) {
            OauthTokens(
                response.access_token,
                response.refresh_token,
                response.id_token
            )
        } else {
            null
        }
    }