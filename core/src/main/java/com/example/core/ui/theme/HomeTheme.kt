package com.example.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private val LightColorPalette =
    lightColorScheme(
        primary = SelectedTabBottomBar,
        secondary = Black,
        background = BackgroundColorLight,
    )

private val DarkColorPalette =
    darkColorScheme(
        primary = SelectedTabBottomBar,
        secondary = Black,
        background = BackgroundColorNight,
    )

@Composable
fun HomeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme =
        remember {
            when {
                darkTheme -> DarkColorPalette
                else -> LightColorPalette
            }
        }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
