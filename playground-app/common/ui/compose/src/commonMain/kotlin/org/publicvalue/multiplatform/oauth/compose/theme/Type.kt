package org.publicvalue.multiplatform.oauth.compose.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

val Typography: Typography
    @Composable get() {
        return Typography(
            headlineLarge = MaterialTheme.typography.headlineLarge.copy(
//                fontSize = 24.sp
            ),
            headlineMedium = MaterialTheme.typography.headlineMedium.copy(
//                fontSize = 18.sp
            ),
            headlineSmall = MaterialTheme.typography.headlineSmall.copy(
//                fontSize = 16.sp
            ),
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
//                fontSize = 14.sp
            ),
            bodyMedium = MaterialTheme.typography.bodyMedium.copy(
//                fontSize = 14.sp
            ),
            bodySmall = MaterialTheme.typography.bodySmall.copy(
//                fontSize = 14.sp
            ),
            titleLarge = MaterialTheme.typography.titleLarge.copy(
//                fontSize = 10.sp
            ),
            titleMedium = MaterialTheme.typography.titleMedium.copy(
//                fontSize = 10.sp
            ),
            titleSmall = MaterialTheme.typography.titleSmall.copy(
//                fontSize = 10.sp
            ),
        )
    }
