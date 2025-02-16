package org.publicvalue.multiplatform.oidc.sample.home

import kotlinx.browser.sessionStorage
import kotlinx.browser.window
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient
import org.publicvalue.multiplatform.oidc.OpenIdConnectException
import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.flows.Pkce
import org.publicvalue.multiplatform.oidc.settings.WasmJsSettingsStore
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

const val OAUTH_STATE = "oauth_state"
const val OAUTH_CODE_VERIFIER = "oauth_code_verifier"

actual suspend fun login(authFlowFactory: CodeAuthFlowFactory, client: OpenIdConnectClient, updateTokenResponse: suspend (AccessTokenResponse) -> Unit) {
    val request = client.createAuthorizationCodeRequest()
    WasmJsSettingsStore(sessionStorage).put(OAUTH_STATE, request.state)
    WasmJsSettingsStore(sessionStorage).put(OAUTH_CODE_VERIFIER, request.pkce.codeVerifier)

    window.location.replace(request.url.toString())
}

actual suspend fun redirect(
    client: OpenIdConnectClient,
    state: String,
    code: String,
    updateTokenResponse: suspend (AccessTokenResponse) -> Unit
) {
    val oldState: String? = WasmJsSettingsStore(sessionStorage).get(OAUTH_STATE)
    val codeVerifier: String? = WasmJsSettingsStore(sessionStorage).get(OAUTH_CODE_VERIFIER)

    if (oldState == null || oldState != state) {
        throw OpenIdConnectException.AuthenticationFailure("Invalid state")
    }

    if(codeVerifier == null) {
        throw IllegalStateException("Invalid code verifier")
    }

    val request = client.createAuthorizationCodeRequest().copy(
        pkce = Pkce(
            codeChallengeMethod = client.config.codeChallengeMethod,
            codeVerifier = codeVerifier
        )
    )

    val tokens = client.exchangeToken(request, code)

    updateTokenResponse(tokens)
}