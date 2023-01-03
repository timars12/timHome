package com.example.core.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val Colors = lightColors(
    primary = SelectedTabBottomBar,
)

@Composable
fun HomeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colors = Colors, content = content)
}