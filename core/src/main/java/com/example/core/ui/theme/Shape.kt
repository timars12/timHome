package com.example.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

data class Shape(
    val default: RoundedCornerShape = RoundedCornerShape(12.dp),
    val micro: RoundedCornerShape = RoundedCornerShape(8.dp),
    val small: RoundedCornerShape = RoundedCornerShape(10.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(16.dp),
    val large: RoundedCornerShape = RoundedCornerShape(30.dp)
)

val LocalShape = compositionLocalOf { Shape() }

val MaterialTheme.cornerRoundedShapes: Shape
    @Composable
    @ReadOnlyComposable
    get() = LocalShape.current
