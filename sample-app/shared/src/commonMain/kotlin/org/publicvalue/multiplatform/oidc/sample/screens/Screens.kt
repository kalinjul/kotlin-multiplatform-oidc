package org.publicvalue.multiplatform.oidc.sample.screens

import com.slack.circuit.runtime.screen.Screen

@CommonParcelize
internal object HomeScreen : SampleAppScreen(name = "Home()")

@CommonParcelize
internal object ConfigScreen : SampleAppScreen(name = "Config()")

internal abstract class SampleAppScreen(val name: String) : Screen {
    open val arguments: Map<String, *>? = null
}
