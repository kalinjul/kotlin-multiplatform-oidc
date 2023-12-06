package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@ExperimentalOpenIdConnect
@OptIn(ExperimentalObjCName::class)
@ObjCName("TokenStoreProtocol", "TokenStoreProtocol", exact = true)
interface TokenStore {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun getIdToken(): String?

    suspend fun removeAccessToken()
    suspend fun removeRefreshToken()
    suspend fun removeIdToken()

    suspend fun saveTokens(tokens: AccessTokenResponse) {
        saveTokens(
            accessToken = tokens.access_token,
            refreshToken = tokens.refresh_token,
            idToken = tokens.id_token
        )
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?)

    suspend fun removeTokens() {
        removeAccessToken()
        removeIdToken()
        removeRefreshToken()
    }
}