package org.publicvalue.multiplatform.oidc.sample.screens

import com.slack.circuit.runtime.screen.ParcelableScreen

@CommonParcelize
object HomeScreen : SampleAppScreen(name = "Home()")
@CommonParcelize
object ConfigScreen : SampleAppScreen(name = "Config()")

abstract class SampleAppScreen(val name: String) : ParcelableScreen {
    open val arguments: Map<String, *>? = null
}