package org.publicvalue.multiplatform.oauth.root

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.publicvalue.multiplatform.oauth.screens.IdpListScreen

@Preview
@Composable
fun HomePreview() {
    val rootScreen = IdpListScreen
    Scaffold(
        bottomBar = {
        }
    ) { paddings ->
        Row(modifier = Modifier.padding(paddings)) {
            Text("Content")
        }
    }

}