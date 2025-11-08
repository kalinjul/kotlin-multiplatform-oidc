package org.publicvalue.multiplatform.oidc.sample.circuit

import org.publicvalue.multiplatform.oidc.appsupport.CodeAuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.config.ConfigPresenterFactory
import org.publicvalue.multiplatform.oidc.sample.config.ConfigUiFactory
import org.publicvalue.multiplatform.oidc.sample.home.HomePresenterFactory
import org.publicvalue.multiplatform.oidc.sample.home.HomeUiFactory

internal class UiFactories internal constructor() {

    companion object {
        val factories = listOf(
            HomeUiFactory,
            ConfigUiFactory
        )
        fun presenterFactories(authFlowFactory: CodeAuthFlowFactory) = listOf(
            HomePresenterFactory(authFlowFactory),
            ConfigPresenterFactory
        )
    }
}
