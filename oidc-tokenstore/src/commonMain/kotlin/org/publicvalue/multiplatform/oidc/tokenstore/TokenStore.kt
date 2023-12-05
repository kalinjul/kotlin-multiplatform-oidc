package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName("TokenStoreProtocol", "TokenStoreProtocol", exact = true)
abstract class TokenStore {
    abstract suspend fun getAccessToken(): String?
    abstract suspend fun getRefreshToken(): String?
    abstract suspend fun getIdToken(): String?

    abstract suspend fun removeAccessToken()
    abstract suspend fun removeRefreshToken()
    abstract suspend fun removeIdToken()

    abstract suspend fun saveTokens(tokens: AccessTokenResponse, removeIfNull: Boolean = false)

    suspend fun removeTokens() {
        removeAccessToken()
        removeIdToken()
        removeRefreshToken()
    }
}