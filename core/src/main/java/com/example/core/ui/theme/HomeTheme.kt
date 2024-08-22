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
        onTertiary = LightBlack,
        tertiary = TextFieldBackgroundColor,
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

private val DarkColorPalette1 =
    darkColorScheme(
        primary = Color.Red,
        onPrimary = Color.Red,
        primaryContainer = Color.Red,
        onPrimaryContainer = Color.Red,
        inversePrimary = Color.Red,
        secondary = Color.Green,
        onSecondary = Color.Green,
        secondaryContainer = Color.Green,
        onSecondaryContainer = Color.Green,
        tertiary = Color.Red,
        onTertiary = Color.Red,
        tertiaryContainer = Color.Red,
        onTertiaryContainer = Color.Red,
        background = Color.Blue,
        onBackground = Color.Blue,
        surface = Color.Yellow,
        onSurface = Color.Yellow,
        surfaceVariant = Color.Yellow,
        onSurfaceVariant = Color.Yellow,
        surfaceTint = Color.Yellow,
        inverseSurface = Color.Yellow,
        inverseOnSurface = Color.Yellow,
        error = Color.Red,
        onError = Color.Red,
        errorContainer = Color.Red,
        onErrorContainer = Color.Red,
        outline = Color.Cyan,
        outlineVariant = Color.Cyan,
        scrim = Color.Cyan,
        surfaceBright = Color.Yellow,
        surfaceDim = Color.Yellow,
        surfaceContainer = Color.Yellow,
        surfaceContainerHigh = Color.Yellow,
        surfaceContainerHighest = Color.Yellow,
        surfaceContainerLow = Color.Yellow,
        surfaceContainerLowest = Color.Yellow,
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
