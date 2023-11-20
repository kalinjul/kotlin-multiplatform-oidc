package org.publicvalue.multiplatform.oauth.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun OAuthPlaygroundTheme(
    isDark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = Typography,
        shapes = Shapes,
        content = content,
        colorScheme = if (isDark) DarkColors else LightColors
    )
}
