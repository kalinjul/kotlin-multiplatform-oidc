package org.publicvalue.multiplatform.oidc.flows

import io.ktor.http.Url
import org.publicvalue.multiplatform.oidc.types.AuthCodeRequest

object Preferences {
    var lastRequest: AuthCodeRequest? = null
    var resultUri: Url? = null
}