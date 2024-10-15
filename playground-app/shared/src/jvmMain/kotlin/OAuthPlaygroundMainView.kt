package org.publicvalue.multiplatform.meldeapp

import androidx.compose.runtime.Composable
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import org.publicvalue.multiplatform.oauth.inject.CommonWindowComponent
import org.publicvalue.multiplatform.oauth.screens.IdpListScreen

@Composable
fun OauthPlaygroundMainView(component: CommonWindowComponent) {
    val backstack = rememberSaveableBackStack(initialScreens = listOf(IdpListScreen))
    val navigator = rememberCircuitNavigator(backstack) {}

    component.appContent(
        backstack,
        navigator,
        { url ->
            // do nothing at the moment
        }
    )
}