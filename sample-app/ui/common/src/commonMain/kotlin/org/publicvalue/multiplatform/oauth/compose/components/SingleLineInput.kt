package org.publicvalue.multiplatform.oauth.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SingleLineInput(value: String, onValueChange: (String) -> Unit, label: @Composable () -> Unit) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        value = value,
        onValueChange = onValueChange,
        label = label
    )
}

