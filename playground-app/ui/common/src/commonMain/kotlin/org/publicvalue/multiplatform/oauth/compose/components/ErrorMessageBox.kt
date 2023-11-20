package org.publicvalue.multiplatform.oauth.compose.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorMessageBox(
    resetErrorMessage: () -> Unit,
    errorMessage: String?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colorScheme.errorContainer,
        onClick = resetErrorMessage
    ) {
        Text(
            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
            text = errorMessage.orEmpty()
        )
    }
}