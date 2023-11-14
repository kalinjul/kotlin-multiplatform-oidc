package org.publicvalue.multiplatform.oauth.inject

import org.publicvalue.multiplatform.oauth.root.OAuthPlaygroundContent

interface CommonWindowComponent : UIComponent {
    val appContent: OAuthPlaygroundContent
}
