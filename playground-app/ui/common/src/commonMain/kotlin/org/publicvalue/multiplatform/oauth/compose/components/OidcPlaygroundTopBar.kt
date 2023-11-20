package org.publicvalue.multiplatform.oauth.compose.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OidcPlaygroundTopBar(
    title: @Composable () -> Unit,
    navigateUp: () -> Unit = {},
    isRootScreen: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            ProvideTextStyle(typography.headlineMedium) {
                title()
            }
        },
        navigationIcon = {
            if (!isRootScreen)
            {
                IconButton(
                    onClick = navigateUp
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Zurück"
                    )
                }
            }
        },
        actions = actions,
    )
}