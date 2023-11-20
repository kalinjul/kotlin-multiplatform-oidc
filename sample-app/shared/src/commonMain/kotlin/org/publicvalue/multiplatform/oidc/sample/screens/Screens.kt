package org.publicvalue.multiplatform.oidc.sample.screens

import com.slack.circuit.runtime.screen.Screen

@CommonParcelize
object HomeScreen : SampleAppScreen(name = "Home()")
@CommonParcelize
object ConfigScreen : SampleAppScreen(name = "Config()")

abstract class SampleAppScreen(val name: String) : Screen {
    open val arguments: Map<String, *>? = null
}