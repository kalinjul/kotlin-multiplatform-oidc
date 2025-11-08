package org.publicvalue.multiplatform.oidc.okhttp

import android.util.Log
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import org.publicvalue.multiplatform.oidc.ExperimentalOpenIdConnect
import org.publicvalue.multiplatform.oidc.tokenstore.OauthTokens

private const val LOG_TAG = "OIDCAuthenticator"

/**
 * OkHttp Authenticator.
 *
 * If no authorization header present, it will provide an access token on 401.
 *
 * If a 401 is encountered with access token set, it will perform a refresh.
 */
@ExperimentalOpenIdConnect
public abstract class OpenIdConnectAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        // authenticator steps in when 401 is received
        if (response.request.header(HttpHeaders.Authorization) == null) {
            Log.d(
                LOG_TAG,
                "Get token for authenticated call. Got ${response.code} with no Authorization header set"
            )
        } else {
            Log.d(
                LOG_TAG,
                "Get token for authenticated call. Got ${response.code} WITH Authorization header set"
            )
        }
        @Suppress("MagicNumber")
        val accessToken = runBlocking {
            try {
                val token = getAccessToken()
                if (token != null && response.code == 401 &&
                    response.request
                        .header(HttpHeaders.Authorization)
                        ?.contains(token) == true
                ) {
                    // Got 401 -> refresh token
                    Log.d(LOG_TAG, "Refreshing access token as using it returned a 401")
                    val newTokens = refreshTokens(oldAccessToken = token)
                    newTokens?.accessToken
                } else {
                    token
                }
            } catch (e: Exception) {
                Log.d(LOG_TAG, "Error while refreshing token: $e")
                null
            }
        }

        return if (accessToken != null && response.request.header(HttpHeaders.Authorization)
                ?.contains(accessToken) != true
        ) { // != true is correct
            response.request.newBuilder()
                .header(HttpHeaders.Authorization, "Bearer $accessToken")
                .apply { buildRequest(this) }
                .build()
        } else {
            // do not try again after 401 if we cannot get a new access token
            Log.d(LOG_TAG, "Authorization failed")
            onRefreshFailed()
            null
        }
    }

    public abstract suspend fun getAccessToken(): String?
    public abstract suspend fun refreshTokens(oldAccessToken: String): OauthTokens?
    public abstract fun onRefreshFailed()

    /** Override to provide additional configuration for the authenticated request **/
    public open fun buildRequest(builder: Request.Builder) {}
}

@ExperimentalOpenIdConnect
@Suppress("ForbiddenPublicDataClass")
public data class OpenIdConnectAuthenticatorConfig(
    internal var getAccessToken: (suspend () -> String?)? = null,
    internal var refreshTokens: (suspend (oldAccessToken: String) -> OauthTokens?)? = null,
    internal var onRefreshFailed: (() -> Unit)? = null,
    internal var buildRequest: Request.Builder.() -> Unit = {}
) {
    public fun getAccessToken(block: suspend () -> String?) {
        getAccessToken = block
    }

    public fun refreshTokens(block: suspend (oldAccessToken: String) -> OauthTokens) {
        refreshTokens = block
    }

    public fun onRefreshFailed(block: () -> Unit) {
        onRefreshFailed = block
    }

    public fun buildRequest(block: Request.Builder.() -> Unit) {
        buildRequest = block
    }

    internal fun validate() {
        requireNotNull(getAccessToken) {
            "getAccessToken() must be configured"
        }
    }
}

@ExperimentalOpenIdConnect
public fun OpenIdConnectAuthenticator(
    configureBlock: OpenIdConnectAuthenticatorConfig.() -> Unit
): OpenIdConnectAuthenticator {
    val config = OpenIdConnectAuthenticatorConfig()
        .apply { configureBlock() }
    config.validate()
    return object : OpenIdConnectAuthenticator() {
        override suspend fun getAccessToken(): String? {
            return config.getAccessToken?.invoke()
        }

        override suspend fun refreshTokens(oldAccessToken: String): OauthTokens? {
            return config.refreshTokens?.invoke(oldAccessToken)
        }

        override fun onRefreshFailed() {
            config.onRefreshFailed?.invoke()
        }

        override fun buildRequest(builder: Request.Builder) {
            config.buildRequest(builder)
        }
    }
}
