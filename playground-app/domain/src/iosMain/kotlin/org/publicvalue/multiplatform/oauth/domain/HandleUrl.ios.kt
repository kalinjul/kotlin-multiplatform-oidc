package org.publicvalue.multiplatform.oauth.domain

import io.ktor.http.Url
import me.tatarka.inject.annotations.Inject

@Inject
actual class HandleUrl {
    actual operator fun invoke(uri: Url) {
    }
}