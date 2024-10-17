package org.publicvalue.multiplatform.oauth.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.CircuitCompositionLocals
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.runtime.Navigator
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.publicvalue.multiplatform.oauth.compose.LocalDateFormatter
import org.publicvalue.multiplatform.oauth.compose.theme.OAuthPlaygroundTheme
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.root.navigation.OAuthPlaygroundNavigator
import org.publicvalue.multiplatform.oauth.screens.IdpListScreen
import org.publicvalue.multiplatform.oauth.strings.DateFormatter

typealias OAuthPlaygroundContent = @Composable (
    onOpenUrl: (String) -> Unit
) -> Unit

@Inject
@Composable
fun OAuthPlaygroundContent(
    @Assisted onOpenUrl: (String) -> Unit,
    rootViewModelFactory: (CoroutineScope) -> RootViewModel,
    circuit: Circuit,
    dateFormatter: DateFormatter,
    logger: Logger,
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { rootViewModelFactory(coroutineScope) }

    val backstack = rememberSaveableBackStack(listOf(IdpListScreen))
    val navigator = rememberCircuitNavigator(backstack, onRootPop = {})

    val DMANavigator: Navigator = remember(navigator) {
        OAuthPlaygroundNavigator(navigator, backstack, onOpenUrl, logger)
    }

    CompositionLocalProvider(
        LocalDateFormatter provides dateFormatter,
    ) {
        CircuitCompositionLocals(circuit) {
            OAuthPlaygroundTheme {
                Root(
                    backstack = backstack,
                    navigator = DMANavigator,
                    logger = logger
                )
            }
        }
    }

}


