package org.publicvalue.multiplatform.meldeapp

import androidx.compose.runtime.Composable
import org.publicvalue.multiplatform.oauth.inject.CommonWindowComponent

@Composable
fun OauthPlaygroundMainView(component: CommonWindowComponent) {
    component.appContent(
        { url ->
            // do nothing at the moment
        }
    )
}