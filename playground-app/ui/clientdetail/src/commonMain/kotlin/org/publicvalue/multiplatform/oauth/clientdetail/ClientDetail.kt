package org.publicvalue.multiplatform.oauth.clientdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.components.ColumnHeadline
import org.publicvalue.multiplatform.oauth.compose.components.ErrorMessageBox
import org.publicvalue.multiplatform.oauth.compose.components.FormHeadline
import org.publicvalue.multiplatform.oauth.compose.components.OidcPlaygroundTopBar
import org.publicvalue.multiplatform.oauth.compose.components.SingleLineInput
import org.publicvalue.multiplatform.oauth.data.db.Client
import org.publicvalue.multiplatform.oauth.data.types.CodeChallengeMethod
import org.publicvalue.multiplatform.oauth.domain.Constants
import org.publicvalue.multiplatform.oauth.screens.ClientDetailScreen
import org.publicvalue.multiplatform.oidc.types.remote.ErrorResponse
import org.publicvalue.multiplatform.oidc.types.remote.AccessTokenResponse

@Inject
class ClientDetailUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is ClientDetailScreen -> {
            ui<ClientDetailUiState> { state, modifier ->
                ClientDetail(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun ClientDetail(
    state: ClientDetailUiState,
    modifier: Modifier = Modifier,
) {
    ClientDetail(
        modifier = modifier,
        client = state.client,
        onNavigateUp = {
            state.eventSink(ClientDetailUiEvent.NavigateUp)
        },
        onNameChange = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::name, it))
        },
        onClientIdChange = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::client_id, it))
        },
        onClientSecretChange =  {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::client_secret, it))
        },
        onCodeChallengeMethodClick = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::code_challenge_method, it))
        },
        onScopeChange = {
            state.eventSink(ClientDetailUiEvent.ChangeClientProperty(Client::scope, it))
        },
        onLogin = {
            state.eventSink(ClientDetailUiEvent.Login)
        },
        errorMessage = state.errorMessage,
        resetErrorMessage = {
            state.eventSink(ClientDetailUiEvent.ResetErrorMessage)
        },
        authcodeRequestUrl = state.authcodeRequestUrl,
        authcodeResponseQueryString = state.authcodeResponseQueryString,
        authcode = state.authcode,
        tokenRequestParameters = state.tokenRequestParameters,
        tokenResponse = state.tokenResponse,
        errorTokenResponse = state.errorTokenResponse,
        tokenResponseStatusCode = state.tokenResponseStatusCode,
    )
}

@Composable
internal fun ClientDetail(
    modifier: Modifier = Modifier,
    client: Client?,
    onNavigateUp: () -> Unit,
    onNameChange: (String) -> Unit,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onScopeChange: (String) -> Unit,
    onCodeChallengeMethodClick: (CodeChallengeMethod) -> Unit,
    onLogin: () -> Unit,
    errorMessage: String?,
    resetErrorMessage: () -> Unit,
    authcodeRequestUrl: String?,
    authcodeResponseQueryString: String?,
    authcode: String?,
    tokenRequestParameters: Parameters?,
    tokenResponse: AccessTokenResponse?,
    errorTokenResponse: ErrorResponse?,
    tokenResponseStatusCode: HttpStatusCode?,
) {
    Scaffold(
        modifier.fillMaxSize(),
        topBar = {
            OidcPlaygroundTopBar(
                title = { Text("Client: ${client?.name.orEmpty()}") },
                isRootScreen = false,
                actions = {
                },
                navigateUp = onNavigateUp
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.navigationBars),
    ) {
        Box(modifier = Modifier.padding(it)) {
            Row(Modifier.padding(16.dp)) {
                Column(Modifier.padding(16.dp).weight(1f)) {
                    ColumnHeadline(text = "Client Configuration")
                    ClientDetail(
                        client = client,
                        onNameChange = onNameChange,
                        onClientIdChange = onClientIdChange,
                        onClientSecretChange = onClientSecretChange,
                        onScopeChange = onScopeChange,
                        onCodeChallengeMethodClick = onCodeChallengeMethodClick
                    )
                }
                Column(Modifier.padding(16.dp).weight(1f)) {
                    ColumnHeadline(text = "Auth Flow")
                    AuthFlow(
                        authcodeRequestUrl = authcodeRequestUrl,
                        authcodeResponseQueryString = authcodeResponseQueryString,
                        authcode = authcode,
                        tokenRequestParameters = tokenRequestParameters,
                        tokenResponse = tokenResponse,
                        errorTokenResponse = errorTokenResponse,
                        tokenResponseStatusCode = tokenResponseStatusCode,
                        onLogin = onLogin,
                    )
                }
            }

            AnimatedVisibility(
                visible = errorMessage != null,
                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
            ) {
                ErrorMessageBox(resetErrorMessage, errorMessage)
            }
        }
    }
}

@Composable
internal fun ClientDetail(
    modifier: Modifier = Modifier,
    client: Client?,
    onNameChange: (String) -> Unit,
    onClientIdChange: (String) -> Unit,
    onClientSecretChange: (String) -> Unit,
    onScopeChange: (String) -> Unit,
    onCodeChallengeMethodClick: (CodeChallengeMethod) -> Unit,
) {
    var name by remember(client?.name != null) {
        mutableStateOf(client?.name.orEmpty())
    }

    var clientId by remember(client?.client_id != null) {
        mutableStateOf(client?.client_id.orEmpty())
    }

    var clientSecret by remember(client?.client_secret != null) {
        mutableStateOf(client?.client_secret.orEmpty())
    }

    var scope by remember(client?.scope != null) {
        mutableStateOf(client?.scope.orEmpty())
    }

    Column(modifier = modifier) {
        FormHeadline(text = "Client")
        SingleLineInput(
            value = name,
            onValueChange = { name = it; onNameChange(it) },
            label = { Text("Name") }
        )
        SingleLineInput(
            value = clientId,
            onValueChange = { clientId = it; onClientIdChange(it)},
            label = { Text("Client ID") }
        )
        SingleLineInput(
            value = clientSecret,
            onValueChange = { clientSecret = it; onClientSecretChange(it)},
            label = { Text("Client secret") }
        )
        FormHeadline(text = "Auth code flow request parameters")
        SingleLineInput(
            value = scope,
            onValueChange = { scope = it; onScopeChange(it)},
            label = { Text("Scope") }
        )
        FormHeadline(text = "Code challenge method")

        CodeChallengeMethod.entries.forEach {
            Row() {
                RadioButton(client?.code_challenge_method == it, onClick = { onCodeChallengeMethodClick(it) })
                Text(modifier = Modifier.padding(start = 16.dp), text = it.name)
            }
        }
//        Row() {
//            RadioButton(false, onClick = null)
//            Text(modifier = Modifier.padding(start = 16.dp), text = "S256")
//        }
//        Row() {
//            RadioButton(false, onClick = null)
//            Text(modifier = Modifier.padding(start = 16.dp), text = "plain")
//        }
//        Row() {
//            RadioButton(false, onClick = null)
//            Text(modifier = Modifier.padding(start = 16.dp), text = "off")
//        }
        Text("Note: redirect_url will always be ${Constants.REDIRECT_URL}")
    }
}

@Composable
internal fun AuthFlow(
    modifier: Modifier = Modifier,
    authcodeRequestUrl: String?,
    authcodeResponseQueryString: String?,
    authcode: String?,
    tokenRequestParameters: Parameters?,
    tokenResponse: AccessTokenResponse?,
    errorTokenResponse: ErrorResponse?,
    tokenResponseStatusCode: HttpStatusCode?,
    onLogin: () -> Unit,
) {
    Column(modifier = modifier) {
        Button(onClick = { onLogin() }) {
            Text("Login")
        }
//        FormHeadline(text = "Discovery")
        if (authcodeRequestUrl != null) {
            FormHeadline(text = "Auth code: $authcode")
            ExpandableInfo(
                label = "Request (length ${authcodeRequestUrl.length})",
                text = authcodeRequestUrl
            )
            ExpandableInfo(
                label = "Response (length ${authcodeResponseQueryString?.length}",
                text = authcodeResponseQueryString.orEmpty(),
                loading = authcodeResponseQueryString == null
            )
        }
        if (tokenRequestParameters != null) {
            FormHeadline(text = "Token exchange")
            ExpandableInfo(
                label = "Request (length ${tokenRequestParameters.format().length})",
                text = tokenRequestParameters.format(),
            )
            ExpandableInfo(
                label = "Response (statusCode $tokenResponseStatusCode)",
                text = tokenResponse?.format() ?: errorTokenResponse?.format().orEmpty(),
                loading = tokenResponse == null && errorTokenResponse == null
            )
            if (tokenResponse != null) {
                FormHeadline(text = "Access Token: ${tokenResponse.access_token}")
                FormHeadline(text = "Refresh Token: ${tokenResponse.refresh_token}")
                FormHeadline(text = "Id Token: ${tokenResponse.id_token?.substring(0,20)?.let { it + "..."}}")
            }
        }
    }
}

@Composable
fun ExpandableInfo(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    loading: Boolean = false
) {
    var expanded by remember() { mutableStateOf(false) }
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = label)
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.padding(horizontal = 8.dp).size(24.dp))
            }
            IconButton(onClick = { expanded = !expanded }) {
                if (expanded) {
                    Icon(imageVector = Icons.Default.ExpandLess, contentDescription = "show less")
                } else {
                    Icon(imageVector = Icons.Default.ExpandMore, contentDescription = "show more")
                }
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Surface(
                color = colorScheme.primaryContainer
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = text,
                    style = typography.bodyMedium
                )
            }
        }
    }
}


fun AccessTokenResponse.format(): String {
    return """
        access_token: $access_token
        refresh_token: $refresh_token
        scope: $scope
        expires_in: $expires_in
        token_type: $token_type
    """.trimIndent()
}

fun ErrorResponse.format(): String {
    return """
        error: $error
        error_description: $error_description
        error_uri: $error_uri
        state: $state
    """.trimIndent()
}

fun Parameters.format(): String {
    return entries().map {
        "${it.key}: ${it.value.joinToString(separator = " ")}"
    }.joinToString(separator = "\n")
}