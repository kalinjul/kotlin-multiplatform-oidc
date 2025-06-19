package org.publicvalue.multiplatform.oidc.sample.config

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
        onValueChange = {
            if (!it.contains("\n")) {
                onValueChange(it)
            }
        },
        label = label
    )
}

