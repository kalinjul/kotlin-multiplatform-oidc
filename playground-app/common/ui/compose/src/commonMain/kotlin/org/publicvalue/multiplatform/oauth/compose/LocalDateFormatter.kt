package org.publicvalue.multiplatform.oauth.compose

import androidx.compose.runtime.staticCompositionLocalOf
import org.publicvalue.multiplatform.oauth.strings.DateFormatter

val LocalDateFormatter = staticCompositionLocalOf<DateFormatter> {
    error("LocalDateFormatter not provided")
}