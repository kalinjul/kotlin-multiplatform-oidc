package org.publicvalue.multiplatform.oauth.root.navigation

import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.PopResult
import com.slack.circuit.runtime.screen.Screen
import kotlinx.collections.immutable.ImmutableList
import org.publicvalue.multiplatform.oauth.logging.Logger

internal class OAuthPlaygroundNavigator(
    private val navigator: Navigator,
    private val backStack: SaveableBackStack,
    private val onOpenUrl: (String) -> Unit,
    private val logger: Logger,
) : Navigator {
    override fun goTo(screen: Screen): Boolean {
        logger.d { "goTo() ${screen::class.simpleName}, backstack: ${backStack.toList().map { it.screen::class.simpleName }}" }

        when (screen) {
//            is UrlScreen -> onOpenUrl(screen.url)
            else -> navigator.goTo(screen)
        }
        return true
    }

    override fun peek(): Screen? {
        return navigator.peek()
    }

    override fun peekBackStack(): ImmutableList<Screen> {
        return navigator.peekBackStack()
    }

    override fun pop(result: PopResult?): Screen? {
        logger.d { "pop(), backstack: ${backStack.toList().map { it.screen::class.simpleName }}}" }
        return navigator.pop()
    }

    override fun resetRoot(
        newRoot: Screen,
        saveState: Boolean,
        restoreState: Boolean
    ): ImmutableList<Screen> {
        logger.d { "resetRoot(), newRoot:$${newRoot::class.simpleName}, backstack: ${backStack.toList().map { it.screen::class.simpleName }}}" }
        return navigator.resetRoot(newRoot)
    }
}