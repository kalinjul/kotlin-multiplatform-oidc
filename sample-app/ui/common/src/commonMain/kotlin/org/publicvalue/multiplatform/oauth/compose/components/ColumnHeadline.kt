package org.publicvalue.multiplatform.oauth.compose.components

import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun ColumnHeadline(text: String) {
    Text(style = typography.headlineSmall, text = text)
}