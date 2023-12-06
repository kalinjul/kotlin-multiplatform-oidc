package org.publicvalue.multiplatform.oidc.tokenstore

import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@ExperimentalOpenIdConnect
@OptIn(ExperimentalObjCName::class)
@ObjCName("TokenStoreProtocol", "TokenStoreProtocol", exact = true)
// not an interface to support extension methods in swift
abstract class TokenStore {
    abstract suspend fun getAccessToken(): String?
    abstract suspend fun getRefreshToken(): String?
    abstract suspend fun getIdToken(): String?

    abstract suspend fun removeAccessToken()
    abstract suspend fun removeRefreshToken()
    abstract suspend fun removeIdToken()

    abstract suspend fun saveTokens(accessToken: String, refreshToken: String?, idToken: String?)
}

// extension method so no need to overwrite in swift subclasses
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun TokenStore.saveTokens(tokens: AccessTokenResponse) {
    saveTokens(
        accessToken = tokens.access_token,
        refreshToken = tokens.refresh_token,
        idToken = tokens.id_token
    )
}

// extension method so no need to overwrite in swift subclasses
@OptIn(ExperimentalOpenIdConnect::class)
suspend fun TokenStore.removeTokens() {
    removeAccessToken()
    removeIdToken()
    removeRefreshToken()
}