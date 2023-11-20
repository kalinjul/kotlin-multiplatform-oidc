package org.publicvalue.multiplatform.oauth.inject

import com.slack.circuit.foundation.Circuit
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.ui.Ui
import org.publicvalue.multiplatform.oauth.idplist.IdpListComponent
import org.publicvalue.multiplatform.oauth.logging.Logger
import me.tatarka.inject.annotations.Provides
import org.publicvalue.multiplatform.oauth.clientdetail.ClientDetailComponent
import org.publicvalue.multiplatform.oauth.clientlist2.ClientListComponent

interface UIComponent:
    IdpListComponent,
    ClientListComponent,
    ClientDetailComponent
{
    @Provides
    @ActivityScope
    fun provideCircuit(
        uiFactories: Set<Ui.Factory>,
        presenterFactories: Set<Presenter.Factory>,
        logger: Logger
    ): Circuit = Circuit.Builder()
        .addUiFactories(uiFactories)
        .addPresenterFactories(presenterFactories)
        .build()
}