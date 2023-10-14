package com.example.home.custom

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import com.example.core.ui.theme.SelectedTabBottomBar
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedAnimatable")
@Composable
fun TestView() {

    val xAnimation =  Animatable(100f)
    val yAnimation =  Animatable(400f)

    LaunchedEffect("animationKey") {
        launch {
            xAnimation.animateTo(1000f, animationSpec = tween(1000))
        }
        launch {
            yAnimation.animateTo(600f, animationSpec = tween(1000))
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        (0..2).forEachIndexed { index, i ->
            drawLine(
                color = SelectedTabBottomBar,
                start = Offset(100f, 400f),
                end = Offset(xAnimation.value, yAnimation.value),
                strokeWidth = 6f
            )
        }
    }
}

data class Pair(val first: Float, val second: Float)

@Composable
fun TestView1() {
    val list = listOf(
        Pair(100f,550f),
        Pair(300f,750f),
        Pair(350f,750f),
//        Pair(700f,900f),
//        Pair(1000f,1200f),
    )

    val xPosition by animateFloatAsState(
        targetValue = list[0].first,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = "",
    )
    val yPosition by animateFloatAsState(
        targetValue = list[0].second,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing), label = "",
    )


    Canvas(modifier = Modifier.fillMaxSize()) {
        list.forEachIndexed { index, pair ->
            val xPreviousPosition = if (index == 0) pair.first else list[index-1].first
            val yPreviousPosition = if (index == 0) pair.second else list[index-1].second
            xPosition.plus(pair.first-xPreviousPosition)
            yPosition.plus(pair.first-yPreviousPosition)
            drawLine(
                color = SelectedTabBottomBar,
                start = Offset(xPreviousPosition, yPreviousPosition),
                end = Offset(xPosition, yPosition),
                strokeWidth = 6f
            )
        }
    }
}

@Preview
@Composable
fun TestViewCheck() {
//    TestView()
    TestView1()
}