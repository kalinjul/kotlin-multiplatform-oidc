package org.publicvalue.multiplatform.oauth.domain

import io.ktor.http.Url
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.data.daos.IdpDao
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.util.DispatcherProvider
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.appsupport.webserver.Webserver
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest
import org.publicvalue.multiplatform.oidc.types.validateState

sealed class AuthorizeResult {
    data class Request(
        val authCodeRequestUrl: String,
        val authCodeRequest: AuthCodeRequest
    ): AuthorizeResult()
    data class Response(
        val authCode: String,
        val authCodeResponseQueryString: String
    ): AuthorizeResult()
}

@OptIn(ExperimentalOpenIdConnect::class)
@Inject
class Authorize(
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val logger: Logger,
    private val urlHandler: HandleUrl,
    private val webserver: Webserver
) {
    suspend operator fun invoke(client: Client): Flow<AuthorizeResult> {
        logger.d { "Login with $client" }

        return flow {
                // TODO discover first?
                val idp = idpDao.getIdp(client.idpId).first()
                val client = client.createOidcClient(idp)

                val request = client.createAuthorizationCodeRequest()
                emit(AuthorizeResult.Request(
                    authCodeRequest = request,
                    authCodeRequestUrl = request.url.toString()
                    )
                )

            val response =
                withContext(dispatchers.io()) {
                    async {
                        urlHandler.invoke(request.url)
                        val response = webserver.startAndWaitForRedirect(Constants.WEBSERVER_PORT, Url(Constants.REDIRECT_URL).encodedPath)
                        webserver.stop()
                        response
                    }.await()
                }

            val authCode = response.parameters.get("code")
            val state = response.parameters.get("state")
            logger.d { "received code: $authCode" }

            emit(
                AuthorizeResult.Response(
                    authCode = authCode ?: "",
                    authCodeResponseQueryString = response.encodedQuery
                )
            )

            if (!request.validateState(state ?: "")) {
                throw Exception("Invalid state")
            }
        }
    }
}
