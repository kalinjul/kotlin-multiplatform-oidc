package org.publicvalue.multiplatform.oauth.clientlist2

import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import org.publicvalue.multiplatform.oauth.inject.ActivityScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface ClientListComponent {
    @IntoSet
    @Provides
    @ActivityScope
    fun bindClientListPresenterFactory(factory: ClientListUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ActivityScope
    fun bindClientListUiFactory(factory: ClientListUiFactory): Ui.Factory = factory
}
