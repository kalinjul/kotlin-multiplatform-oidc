package org.publicvalue.multiplatform.oauth.domain

import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

sealed class RefreshTokenResult {
    data class Request(val parameters: Parameters) : RefreshTokenResult()
    data class Response(
        val httpStatusCode: HttpStatusCode,
        val accessTokenResponse: AccessTokenResponse?,
        val cause: Exception? = null,
    ) : RefreshTokenResult()
}

@Inject
class RefreshToken(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
) {
    suspend operator fun invoke(client: Client, refreshToken: String): Flow<RefreshTokenResult> {
        val idp = idpDao.getIdp(client.idpId).first()
        val oidcClient = client.createOidcClient(idp)

        return flow {
            val request = oidcClient.createRefreshTokenRequest(refreshToken)
            emit(RefreshTokenResult.Request(request.formParameters))
            try {
                val result = withContext(dispatchers.io()) {
                    oidcClient.refreshToken(refreshToken)
                }
                emit(RefreshTokenResult.Response(HttpStatusCode.OK, result))
            } catch (e: OpenIdConnectException.UnsuccessfulTokenRequest) {
                emit(RefreshTokenResult.Response(e.statusCode, null, e.cause))
                throw e
            }
        }
    }
}
