package org.publicvalue.multiplatform.oauth.inject

import me.tatarka.inject.annotations.Component

@ActivityScope
@Component
abstract class IosUiComponent(
    @Component val applicationComponent: IosApplicationComponent,
) : CommonIosUiComponent {

    companion object
}
