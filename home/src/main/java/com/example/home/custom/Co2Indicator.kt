package com.example.home.custom

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.*
import com.example.core.utils.Constant.INDICATOR_CO2_ACCEPTABLE_VALUE
import com.example.core.utils.Constant.INDICATOR_CO2_BED_LEVEL
import com.example.core.utils.Constant.INDICATOR_CO2_GOOD_LEVEL
import com.example.core.utils.Constant.INDICATOR_CO2_LOW_DANGER_LEVEL
import com.example.core.utils.Constant.INDICATOR_CO2_UNCOMFORTABLE_LEVEL
import okhttp3.internal.toImmutableList

private const val MILLIS_DURATION_FOR_CO2_INDICATOR = 3000
private const val MILLIS_DURATION_FOR_TEXT_CO2_INDICATOR = 180
private const val SIZE_INNER_CIRCLE_RATIO = .94F
private const val OFFSET_INNER_CIRCLE_RATIO = .03F

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Co2Indicator(modifier: Modifier, co2: Int, isAnimated: Boolean) {
    val countOfIteration = remember(co2) { getCountByCO2(co2) }
    val listColors = listOf(
        IndicatorCO2Good,
        IndicatorCO2Acceptable,
        IndicatorCO2Uncomfortable,
        IndicatorCO2Bed,
        IndicatorCO2LowDanger,
        IndicatorCO2Danger
    ).toImmutableList()
    val indicatorColor = remember(co2) { listColors[countOfIteration] }

    val sweepAngle by animateFloatAsState(
        targetValue = if (isAnimated) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = LinearEasing)
    )

    val co2ColorIndicator = remember { Animatable(initialValue = listColors[0]) }
    LaunchedEffect(key1 = co2) {
        co2ColorIndicator.animateTo(
            targetValue = indicatorColor,
            animationSpec = keyframes {
                if (countOfIteration == 0) return@keyframes
                durationMillis = MILLIS_DURATION_FOR_CO2_INDICATOR
                val secondFrame = durationMillis / countOfIteration
                for (secondColor in 0..countOfIteration) {
                    listColors[secondColor] at secondFrame with FastOutLinearInEasing
                }
            }
        )
    }

    Box {
        Canvas(
            modifier = modifier.fillMaxSize()
        ) {
            drawArc(
                color = co2ColorIndicator.value,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = Size(size.width, size.height)
            )
            drawArc(
                color = Color.White,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = true,
                size = Size(
                    size.width * SIZE_INNER_CIRCLE_RATIO,
                    size.height * SIZE_INNER_CIRCLE_RATIO
                ),
                topLeft = Offset(
                    size.width * OFFSET_INNER_CIRCLE_RATIO,
                    size.height * OFFSET_INNER_CIRCLE_RATIO
                )
            )
        }
        AnimatedVisibility(
            modifier = Modifier.align(Alignment.Center),
            visible = isAnimated,
            enter = remember {
                scaleIn(
                    animationSpec = tween(
                        durationMillis = MILLIS_DURATION_FOR_TEXT_CO2_INDICATOR,
                        easing = LinearEasing
                    )
                )
            }
        ) {
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
                    text = "co2", // TODO
                    color = TextColorCO2,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Suppress("MagicNumber")
private fun getCountByCO2(co2: Int): Int {
    return when {
        co2 <= INDICATOR_CO2_GOOD_LEVEL -> 0
        co2 <= INDICATOR_CO2_ACCEPTABLE_VALUE -> 1
        co2 <= INDICATOR_CO2_UNCOMFORTABLE_LEVEL -> 2
        co2 <= INDICATOR_CO2_BED_LEVEL -> 3
        co2 <= INDICATOR_CO2_LOW_DANGER_LEVEL -> 4
        else -> 5
    }
}

@Preview
@Composable
@Suppress("MagicNumber")
fun PreviewCo2Indicator() {
    Co2Indicator(Modifier.size(150.dp), 2500, true)
}
