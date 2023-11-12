package org.publicvalue.multiplatform.oauth.inject

import me.tatarka.inject.annotations.Component

@Component
@ApplicationScope
abstract class DesktopApplicationComponent(
) : CommonDesktopApplicationComponent {

    companion object
}
