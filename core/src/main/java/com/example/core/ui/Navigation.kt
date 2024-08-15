package com.example.core.ui

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

private const val ANIMATION_DURATION_MILLS = 220
private const val ANIMATION_DELAY_MILLS = 90

fun slideIntoContainer(): EnterTransition {
    return fadeIn(animationSpec = tween(ANIMATION_DURATION_MILLS)) + slideInHorizontally(animationSpec = tween(ANIMATION_DURATION_MILLS))
}

fun slideOutOfContainer(): ExitTransition {
    return fadeOut(animationSpec = tween(ANIMATION_DURATION_MILLS)) + slideOutHorizontally(animationSpec = tween(durationMillis = ANIMATION_DURATION_MILLS))
}

fun scaleIntoContainer(): EnterTransition {
    return fadeIn(animationSpec = tween(ANIMATION_DURATION_MILLS, delayMillis = ANIMATION_DELAY_MILLS)) +
        scaleIn(
            animationSpec = tween(ANIMATION_DURATION_MILLS, delayMillis = ANIMATION_DELAY_MILLS),
            initialScale = 1.1f,
        )
}

fun scaleOutOfContainer(): ExitTransition {
    return fadeOut(tween(delayMillis = ANIMATION_DELAY_MILLS)) +
        scaleOut(
            animationSpec = tween(durationMillis = ANIMATION_DURATION_MILLS, delayMillis = ANIMATION_DELAY_MILLS),
            targetScale = 1.1f,
        )
}
