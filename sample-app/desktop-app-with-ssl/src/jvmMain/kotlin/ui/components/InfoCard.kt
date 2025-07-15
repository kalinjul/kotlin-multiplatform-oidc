package ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ui.theme.InfoRow
import ui.theme.SectionTitle

@Composable
fun InfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionTitle(title)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun StatusInfoCard(
    title: String,
    statusItems: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = title,
        modifier = modifier
    ) {
        statusItems.forEach { (label, value) ->
            InfoRow(label = label, value = value)
        }
    }
}

@Composable
fun ConfigurationCard(
    title: String,
    configCode: String,
    modifier: Modifier = Modifier
) {
    InfoCard(
        title = title,
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = configCode,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp),
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}