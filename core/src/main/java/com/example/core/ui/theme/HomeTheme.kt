package com.example.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

private val LightColorPalette =
    lightColorScheme(
        primary = SelectedTabBottomBar,
        onPrimary = Color.White,
        inversePrimary = Color.Black,
        secondary = Black,
        background = BackgroundColorLight,
        onBackground = Color.White,
        onTertiary = LightBlack,
        tertiary = TextFieldBackgroundColor,
        onSurface = TextColorCO2,
    )

private val DarkColorPalette =
    darkColorScheme(
        primary = SelectedTabBottomBar,
        onPrimary = Color.Black,
        inversePrimary = BackgroundColorLight,
        secondary = Black,
        background = BackgroundColorNight,
        // color Text
        onBackground = Black,
        onTertiary = LightBlack,
        tertiary = TextColorCO2,
        surface = BackgroundColor,
        // buttons(and text in btn) & bnm
        onSurface = LightBlack,
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
