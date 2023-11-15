package org.publicvalue.multiplatform.oauth.domain

import io.ktor.http.Url
import io.ktor.http.toURI
import me.tatarka.inject.annotations.Inject
import java.awt.Desktop

@Inject
actual class HandleUrl {
    actual operator fun invoke(uri: Url) {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri.toURI())
            } catch (e: Exception) {
                e.printStackTrace()
                throw UrlOpenException(e.message, cause = e)
            }
        } else {
            throw UrlOpenException("Desktop does not support Browse Action")
        }
    }
}