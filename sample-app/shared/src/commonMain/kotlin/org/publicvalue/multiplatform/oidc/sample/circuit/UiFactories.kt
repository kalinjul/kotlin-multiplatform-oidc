package org.publicvalue.multiplatform.oidc.sample.circuit

import org.publicvalue.multiplatform.oidc.appsupport.AuthFlowFactory
import org.publicvalue.multiplatform.oidc.sample.config.ConfigPresenterFactory
import org.publicvalue.multiplatform.oidc.sample.config.ConfigUiFactory
import org.publicvalue.multiplatform.oidc.sample.home.HomePresenterFactory
import org.publicvalue.multiplatform.oidc.sample.home.HomeUiFactory


class UiFactories {

    companion object {
        val uiFactories = listOf(
            HomeUiFactory, ConfigUiFactory
        )
        fun presenterFactories(authFlowFactory: AuthFlowFactory) = listOf(
            HomePresenterFactory(authFlowFactory), ConfigPresenterFactory
        )
    }
}

