package org.publicvalue.multiplatform.oidc.sample.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import io.ktor.http.*
import kotlinx.browser.window

@Composable
actual fun HandleRedirect(state: HomeUiState) {
    LaunchedEffect(Unit) {
        val url = Url(window.location.toString())

        val parameters = url.parameters

        val stateParam = parameters["state"]
        val codeParam = parameters["code"]
        if(stateParam != null && codeParam != null) {
            state.eventSink(HomeUiEvent.Redirect(state = stateParam, code = codeParam))
        }
    }
}