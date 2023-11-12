package org.publicvalue.multiplatform.oauth.idplist

import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import org.publicvalue.multiplatform.oauth.inject.ActivityScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface IdpListComponent {
    @IntoSet
    @Provides
    @ActivityScope
    fun bindIdpListPresenterFactory(factory: IdpListUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ActivityScope
    fun bindIdpListUiFactory(factory: IdpListUiFactory): Ui.Factory = factory
}
