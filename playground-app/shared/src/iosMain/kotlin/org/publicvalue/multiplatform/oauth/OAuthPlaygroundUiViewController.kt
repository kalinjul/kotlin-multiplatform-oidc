package org.publicvalue.multiplatform.oauth

import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import org.publicvalue.multiplatform.oauth.root.OAuthPlaygroundContent
import org.publicvalue.multiplatform.oauth.screens.IdpListScreen
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIViewController

typealias OauthPlaygroundUiViewController = () -> UIViewController

@Inject
fun OauthPlaygroundUiViewController(
    OAuthPlaygroundContent: OAuthPlaygroundContent,
): UIViewController = ComposeUIViewController {
    OAuthPlaygroundContent(
        { url ->
            // do nothing at the moment
        }
    )
}