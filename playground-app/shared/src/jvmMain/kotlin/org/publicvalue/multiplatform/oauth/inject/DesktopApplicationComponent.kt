package org.publicvalue.multiplatform.oauth.inject

import me.tatarka.inject.annotations.Component
import org.publicvalue.multiplatform.oauth.domain.inject.WebserverComponent

@Component
@ApplicationScope
abstract class DesktopApplicationComponent(
) : CommonDesktopApplicationComponent, WebserverComponent {

    companion object
}
