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
import org.publicvalue.multiplatform.oidc.types.ErrorResponse
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.OpenIDConnectException
import org.publicvalue.multiplatform.oidc.types.AccessTokenResponse

sealed class ExchangeTokenResult {
    data class Request(
        val parameters: Parameters
    ): ExchangeTokenResult()
    data class Response(
        val httpStatusCode: HttpStatusCode,
        val accessTokenResponse: AccessTokenResponse?,
        val errorResponse: ErrorResponse? = null
    ): ExchangeTokenResult()
}

@Inject
class ExchangeToken(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
) {
    suspend operator fun invoke(client: Client, request: AuthCodeRequest, code: String): Flow<ExchangeTokenResult> {
        logger.d { "Exchange token with $client, authCode: $code" }

        val idp = idpDao.getIdp(client.idpId).first()
        val client = client.createOidcClient(idp)

        return flow {
            val (_, requestParams) = client.createAccessTokenRequest(request, code)
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
            } catch (e: OpenIDConnectException.UnsuccessfulTokenRequest) {
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

