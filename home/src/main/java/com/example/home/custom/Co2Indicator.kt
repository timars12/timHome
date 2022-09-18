package com.example.home.custom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun Co2Indicator(modifier: Modifier, co2: Int) {
    val indicatorColor = remember(co2) { getColorByCO2(co2) }

    Box {
        Canvas(
            modifier = modifier
                .fillMaxSize()
        ) {
            drawCircle(
                color = indicatorColor,
                style = Stroke(width = 15f, cap = StrokeCap.Round)
            )
        }
        Column(
            modifier = Modifier.align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = co2.toString(),
                color = indicatorColor,
                fontWeight = FontWeight.W900,
                fontSize = 16.sp
            )
            Text(
                text = "co2",
                color = Color(0xff9B9B9B),
                fontSize = 12.sp
            )
        }
    }
}

private fun getColorByCO2(co2: Int): Color {
    return when {
        co2 <= 400 -> Color(0xff92DB90)
        co2 <= 600 -> Color(0xffEAE682)
        co2 <= 800 -> Color(0xffE3AC41)
        else -> Color(0xffEB736C)
    }
}
