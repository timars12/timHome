package com.timhome.core.ui

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

private const val ANIMATION_DURATION_MILLS = 220
private const val ANIMATION_DELAY_MILLS = 90
private const val TRANSITION_STYLE_KEY = "com.timhome.core.ui.transitionStyle"

/** The animation style a screen entry declares. The app picks the animation from it. */
enum class TransitionStyle { Slide, Scale }

/** Entry metadata for a screen that slides in. */
fun slideTransition(): Map<String, Any> = mapOf(TRANSITION_STYLE_KEY to TransitionStyle.Slide)

/** Entry metadata for a screen that scales in. */
fun scaleTransition(): Map<String, Any> = mapOf(TRANSITION_STYLE_KEY to TransitionStyle.Scale)

/** The style set by [slideTransition] or [scaleTransition], or `null` when the entry has none. */
fun Map<String, Any>.transitionStyle(): TransitionStyle? = this[TRANSITION_STYLE_KEY] as? TransitionStyle

fun TransitionStyle.toContentTransform(): ContentTransform =
    when (this) {
        TransitionStyle.Slide -> slideIntoContainer() togetherWith slideOutOfContainer()
        TransitionStyle.Scale -> scaleIntoContainer() togetherWith scaleOutOfContainer()
    }

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
