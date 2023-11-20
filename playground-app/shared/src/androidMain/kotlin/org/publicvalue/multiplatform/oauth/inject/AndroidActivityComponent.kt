package org.publicvalue.multiplatform.oauth.inject

import android.app.Activity
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides

@ActivityScope
@Component
abstract class AndroidActivityComponent(
    @get:Provides override val activity: Activity,
    @Component val applicationComponent: AndroidApplicationComponent,
) : CommonAndroidActivityComponent {
    companion object
}
