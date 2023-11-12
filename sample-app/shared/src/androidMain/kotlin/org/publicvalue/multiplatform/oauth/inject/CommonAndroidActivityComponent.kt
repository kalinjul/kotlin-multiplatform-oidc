package org.publicvalue.multiplatform.oauth.inject

import android.app.Activity
import org.publicvalue.multiplatform.oauth.root.OAuthPlaygroundContent

interface CommonAndroidActivityComponent : UIComponent {
    val appContent: OAuthPlaygroundContent

    val activity: Activity
}
