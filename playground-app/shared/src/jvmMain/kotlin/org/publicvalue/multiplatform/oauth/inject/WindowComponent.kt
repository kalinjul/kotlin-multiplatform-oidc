package org.publicvalue.multiplatform.oauth.inject

import me.tatarka.inject.annotations.Component

@ActivityScope
@Component
abstract class WindowComponent(
    @Component val applicationComponent: DesktopApplicationComponent,
) : CommonWindowComponent {
    companion object
}
