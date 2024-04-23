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
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import org.publicvalue.multiplatform.oidc.types.AuthRequest
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.types.remote.AuthResult

sealed class ExchangeTokenResult {
    data class Request(
        val parameters: Parameters
    ): ExchangeTokenResult()
    data class Response(
        val httpStatusCode: HttpStatusCode,
        val accessTokenResponse: AuthResult.AccessToken?,
        val errorResponse: ErrorResponse? = null
    ): ExchangeTokenResult()
}

@Inject
class ExchangeToken(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) {
    suspend operator fun invoke(client: Client, request: AuthRequest.Code, code: String): Flow<ExchangeTokenResult> {
        logger.d { "Exchange token with $client, authCode: $code" }

        val idp = idpDao.getIdp(client.idpId).first()
        val client = client.createOidcClient(idp)

        return flow {
            val requestParams = client.createAccessTokenRequest(request, code).parameters
            emit(
                ExchangeTokenResult.Request(
                    parameters = requestParams
                )
            )
            try {
                val result = withContext(dispatchers.io()) {
                    client.exchangeToken(request, code)
                }
                println(result)
                emit(
                    ExchangeTokenResult.Response(
                        httpStatusCode = HttpStatusCode.OK,
                        accessTokenResponse = result,
                    )
                )
            } catch (e: OpenIdConnectException.UnsuccessfulTokenRequest) {
                emit(
                    ExchangeTokenResult.Response(
                        httpStatusCode = e.statusCode,
                        accessTokenResponse = null,
                        errorResponse = e.errorResponse
                    )
                )
                throw e
            }
        }
    }
}

