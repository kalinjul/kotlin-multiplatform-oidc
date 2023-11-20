package org.publicvalue.multiplatform.oauth.inject

import org.publicvalue.multiplatform.oauth.OauthPlaygroundUiViewController
import me.tatarka.inject.annotations.Provides
import platform.UIKit.UIViewController

interface CommonIosUiComponent : UIComponent {

    val uiViewControllerFactory: () -> UIViewController

    @Provides
    @ActivityScope
    fun uiViewController(bind: OauthPlaygroundUiViewController): UIViewController = bind()
}
