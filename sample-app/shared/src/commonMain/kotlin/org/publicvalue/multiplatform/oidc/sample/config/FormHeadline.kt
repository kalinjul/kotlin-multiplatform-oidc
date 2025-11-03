package org.publicvalue.multiplatform.oidc.sample.config

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun FormHeadline(
    modifier:Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier.padding(vertical = 16.dp),
        style = typography.titleSmall, text = text
    )
}