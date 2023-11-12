package org.publicvalue.multiplatform.oauth.inject

import android.app.Application
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@Component
@ApplicationScope
abstract class AndroidApplicationComponent(
    @get:Provides override val application: Application
): CommonAndroidApplicationComponent {
    companion object
}