package org.publicvalue.multiplatform.oauth.root

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.isAtRoot
import com.slack.circuit.foundation.NavigableCircuitContent
import com.slack.circuit.overlay.ContentWithOverlays
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import org.publicvalue.multiplatform.oauth.logging.Logger
import org.publicvalue.multiplatform.oauth.screens.isRootScreen


internal enum class NavigationType {
    BOTTOM_NAVIGATION,
//    RAIL,
//    PERMANENT_DRAWER,
}
@Composable
fun Root(
    backstack: SaveableBackStack,
    navigator: Navigator,
    logger: Logger
) {
    val rootScreen by remember(backstack) {
        derivedStateOf { backstack.last().screen }
    }
    val currentTabScreen by remember(backstack) {
        derivedStateOf { backstack.toList().find { it.screen.isRootScreen() }?.screen ?: backstack.last().screen}
    }

    val navigationItems = remember(Unit) { buildNavigationItems() }

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
                ContentWithOverlays {
                    val overlayHost = LocalOverlayHost.current
                    val scope = rememberCoroutineScope()

                    NavigableCircuitContent(
                        navigator = navigator,
                        backstack = backstack,
//                        decoration = remember(navigator) {
//                            GestureNavigationDecoration(onBackInvoked = navigator::pop) // only for api 33
//                        },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )
                }
            }
        }
    }
}

private fun Navigator.navigateToRootScreen(
    screen: Screen,
    backstack: SaveableBackStack,
) {
    if (!backstack.isAtRoot || backstack.topRecord?.screen != screen) {
        resetRoot(screen)
    }
}


@Immutable
internal data class HomeNavigationItem(
    val screen: Screen,
    val label: String,
    val contentDescription: String,
    val iconImageVector: ImageVector,
    val selectedImageVector: ImageVector? = null,
)

internal fun buildNavigationItems(): List<HomeNavigationItem> {
    return listOf(
//        HomeNavigationItem(
//            screen = ReportScreen,
//            label = "Meldungen",
//            contentDescription = "Meldungen",
//            iconImageVector = Icons.Outlined.List,
//            selectedImageVector = Icons.Filled.List,
//        ),
//        HomeNavigationItem(
//            screen = ProfileScreen,
//            label = "Meine Daten",
//            contentDescription = "Meine Daten",
//            iconImageVector = Icons.Outlined.AccountCircle,
//            selectedImageVector = Icons.Filled.AccountCircle,
//        ),
    )
}