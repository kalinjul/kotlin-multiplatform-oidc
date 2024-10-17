package org.publicvalue.multiplatform.oidc.sample

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.foundation.Circuit
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.runtime.Navigator
import org.publicvalue.multiplatform.oidc.sample.data.LocalSettingsStore
import org.publicvalue.multiplatform.oidc.sample.data.OidcSettingsStore
import org.publicvalue.multiplatform.oidc.settings.SettingsStore


//val DMANavigator: Navigator = remember(navigator) {
//    OAuthPlaygroundNavigator(navigator, backstack, onOpenUrl, logger)
//}

@Composable
fun Root(
    circuit: Circuit,
    navigator: Navigator,
    backstack: SaveableBackStack,
    settingsStore: SettingsStore,
) {
    val oidcSettingsStore = remember(settingsStore) { OidcSettingsStore(settingsStore) }
    Scaffold(
        bottomBar = {
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
        .exclude(WindowInsets.statusBars)
    //            .exclude(WindowInsets.navigationBars),
        ) { paddingValues ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row {
                    CompositionLocalProvider(LocalSettingsStore provides oidcSettingsStore) {
                        NavigableCircuitContent(
                            circuit = circuit,
                            navigator = navigator,
                            backStack = backstack,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    }
                }
            }
        }
}