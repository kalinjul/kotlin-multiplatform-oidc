package org.publicvalue.multiplatform.oauth.domain

import io.ktor.http.HttpStatusCode
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

sealed class EndSessionResult {
    data class Request(
        val endSessionRequestUrl: String,
    ): EndSessionResult()
    data class Response(val statusCode: HttpStatusCode?): EndSessionResult()
}

@OptIn(ExperimentalOpenIdConnect::class)
@Inject
class LogoutWebFlow(
    private val logger: Logger,
    private val idpDao: IdpDao,
    private val dispatchers: DispatcherProvider,
    private val urlHandler: HandleUrl,
    private val webserver: Webserver
) {
    suspend operator fun invoke(client: Client, idToken: String): Flow<EndSessionResult> = flow {

        logger.d { "Logout with $client" }

        val idp = idpDao.getIdp(client.idpId).first()
        val client = client.createOidcClient(idp)

        val request = client.createEndSessionRequest(idToken)
        emit(EndSessionResult.Request(
                endSessionRequestUrl = request.url.toString()
            )
        )

        withContext(dispatchers.io()) {
            async {
                urlHandler.invoke(request.url)
                val response = webserver.startAndWaitForRedirect(Constants.WEBSERVER_PORT, Url(Constants.POST_LOGOUT_REDIRECT_URL).encodedPath)
                webserver.stop()
                response
            }.await()
        }

        logger.d { "post_logout_redirect_uri called" }

        emit(
            EndSessionResult.Response(null)
        )
    }
}

@OptIn(ExperimentalOpenIdConnect::class)
@Inject
class LogoutPost(
    private val logger: Logger,
    private val idpDao: IdpDao,
) {
    suspend operator fun invoke(client: Client, idToken: String): Flow<EndSessionResult> = flow {

        logger.d { "Logout with $client" }

        val idp = idpDao.getIdp(client.idpId).first()
        val client = client.createOidcClient(idp)

        emit(EndSessionResult.Request(
                endSessionRequestUrl = ""
            )
        )
        val code = client.endSession(idToken)

        logger.d { "logout response: $code" }

        emit(
            EndSessionResult.Response(code)
        )
    }
}