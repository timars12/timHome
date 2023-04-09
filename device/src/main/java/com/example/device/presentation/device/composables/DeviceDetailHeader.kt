package com.example.device.presentation.device.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.core.ui.theme.LightBlack
import com.example.core.ui.theme.PrimaryColor

typealias CalculateTextSize = () -> TextUnit

@Composable
fun DeviceDetailHeader(
    modifier: Modifier,
    image: String?,
    title: String,
    calculateTextSize: CalculateTextSize
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .background(PrimaryColor),
        contentAlignment = Alignment.BottomStart
    ) {
        Image(
            modifier = modifier.fillMaxSize(),
            painter = rememberAsyncImagePainter(image),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        TextFontChengDynamically(modifier, title, calculateTextSize)
    }
}

@Composable
private fun TextFontChengDynamically(
    modifier: Modifier,
    title: String,
    calculateTextSize: CalculateTextSize
) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(listOf(Color.Transparent, LightBlack)),
        )
    ) {
        Text(
            modifier = Modifier.padding(24.dp),
            text = title,
            color = Color.Black,
            style = MaterialTheme.typography.body2.copy(
                fontWeight = FontWeight.W700,
                fontSize = calculateTextSize()
            )
        )
    }
}
