package org.publicvalue.multiplatform.oauth.ClientDetail

import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import org.publicvalue.multiplatform.oauth.inject.ActivityScope
import me.tatarka.inject.annotations.IntoSet
import me.tatarka.inject.annotations.Provides

interface ClientDetailComponent {
    @IntoSet
    @Provides
    @ActivityScope
    fun bindClientDetailPresenterFactory(factory: ClientDetailUiPresenterFactory): Presenter.Factory = factory

    @IntoSet
    @Provides
    @ActivityScope
    fun bindClientDetailUiFactory(factory: ClientDetailUiFactory): Ui.Factory = factory
}
