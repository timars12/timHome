package com.example.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RatingView(
    modifier: Modifier = Modifier,
    rating: Float,
    imageEmpty: ImageBitmap,
    imageFilled: ImageBitmap,
    itemSize: Dp = Dp.Unspecified,
    animationEnabled: Boolean = true,
    gestureEnabled: Boolean = true,
    itemCount: Int = 5,
    space: Dp = 0.dp,
    onRatingChange: ((Float) -> Unit)? = null,
) {
    val intrinsicWidth = imageEmpty.width.toFloat()
    val intrinsicHeight = imageEmpty.height.toFloat()

    RatingViewImpl(
        modifier = modifier,
        rating = rating,
        intrinsicWidth = intrinsicWidth,
        intrinsicHeight = intrinsicHeight,
        itemSize = itemSize,
        animationEnabled = animationEnabled,
        gestureEnabled = gestureEnabled,
        itemCount = itemCount,
        space = space,
        block = { updatedRating: Float, spaceBetween: Float ->
            drawRatingImages(
                rating = updatedRating,
                itemCount = itemCount,
                imageEmpty = imageEmpty,
                imageFilled = imageFilled,
                space = spaceBetween,
            )
        },
        onRatingChange = onRatingChange,
    )
}

private const val DURATION_RATING = 300

@Composable
private fun RatingViewImpl(
    modifier: Modifier = Modifier,
    rating: Float,
    itemSize: Dp = Dp.Unspecified,
    intrinsicWidth: Float,
    intrinsicHeight: Float,
    animationEnabled: Boolean = true,
    gestureEnabled: Boolean = true,
    itemCount: Int = 5,
    space: Dp = 0.dp,
    block: DrawScope.(
        rating: Float,
        space: Float,
    ) -> Unit,
    onRatingChange: ((Float) -> Unit)? = null,
) {
    Box(modifier) {
        val height: Dp =
            when {
                itemSize != Dp.Unspecified -> itemSize
                else -> LocalDensity.current.run { intrinsicHeight.toDp() }
            }
        val spacePx: Float = LocalDensity.current.run { space.toPx() }
        val itemWidthPx: Float =
            when {
                itemSize != Dp.Unspecified -> LocalDensity.current.run { itemSize.toPx() }
                else -> intrinsicWidth
            }
        val totalWidth: Dp =
            LocalDensity.current.run {
                itemWidthPx.toDp() * itemCount + space * (itemCount - 1)
            }
        val itemIntervals = remember { ratingItemPositions(itemWidthPx, spacePx, itemCount) }
        val coerced = rating.coerceIn(0f, itemCount.toFloat())
        val coroutineScope = rememberCoroutineScope()
        val animatableRating = remember { Animatable(if (animationEnabled) 0f else coerced) }

        LaunchedEffect(key1 = if (gestureEnabled) Unit else coerced) {
            if (animationEnabled) {
                animatableRating.animateTo(
                    targetValue = coerced,
                    animationSpec = tween(DURATION_RATING, easing = LinearEasing),
                )
            } else {
                animatableRating.snapTo(coerced)
            }
        }

        val gestureModifier =
            Modifier
                .pointerInput(Unit) {
                    val ratingBarWidth = size.width.toFloat()
                    detectDragGestures { change, _ ->
                        val x = change.position.x
                        val newRating =
                            getRatingFromTouchPosition(
                                x = x,
                                itemIntervals = itemIntervals,
                                ratingBarDimension = ratingBarWidth,
                                space = spacePx,
                                totalCount = itemCount,
                            )

                        coroutineScope.launch {
                            animatableRating.snapTo(newRating)
                            onRatingChange?.invoke(animatableRating.value)
                        }
                    }
                }
                .pointerInput(Unit) {
                    val ratingBarWidth = size.width.toFloat()

                    detectTapGestures { change ->
                        val x = change.x
                        val newRating =
                            getRatingFromTouchPosition(
                                x = x,
                                itemIntervals = itemIntervals,
                                ratingBarDimension = ratingBarWidth,
                                space = spacePx,
                                totalCount = itemCount,
                            )

                        coroutineScope.launch {
                            if (animationEnabled) {
                                animatableRating.animateTo(
                                    targetValue = newRating,
                                    animationSpec = tween(300, easing = LinearEasing),
                                )
                            } else {
                                animatableRating.snapTo(newRating)
                            }
                            onRatingChange?.invoke(animatableRating.value)
                        }
                    }
                }

        Box(
            modifier =
                Modifier
                    .then(if (gestureEnabled) gestureModifier else Modifier)
                    .width(totalWidth)
                    .height(height)
                    .drawBehind {
                        block(animatableRating.value, spacePx)
                    },
        )
    }
}

private fun DrawScope.drawRatingImages(
    rating: Float,
    itemCount: Int,
    imageEmpty: ImageBitmap,
    imageFilled: ImageBitmap,
    space: Float,
) {
    val imageHeight = size.height
    val ratingInt = rating.toInt()

    drawWithLayer {
        // Draw foreground rating items
        for (i in 0 until itemCount) {
            val start = imageHeight * i + space * i
            // Destination
            translate(left = start, top = 0f) {
                drawImage(
                    image = imageFilled,
                    dstSize = IntSize(size.width.toInt(), imageHeight.toInt()),
                )
            }
        }

        // End of rating bar
        val startOfEmptyItems = imageHeight * itemCount + space * (itemCount - 1)
        // Start of empty rating items
        val endOfFilledItems = rating * imageHeight + ratingInt * space
        // Rectangle width that covers empty items
        val rectWidth = startOfEmptyItems - endOfFilledItems
        // Source
        drawRect(
            Color.Transparent,
            topLeft = Offset(endOfFilledItems, 0f),
            size = Size(rectWidth, height = size.height),
            blendMode = BlendMode.SrcIn,
        )

        for (i in 0 until itemCount) {
            translate(left = imageHeight * i + space * i, top = 0f) {
                drawImage(
                    image = imageEmpty,
                    dstSize = IntSize(size.width.toInt(), imageHeight.toInt()),
                )
            }
        }
    }
}

private fun DrawScope.drawWithLayer(block: DrawScope.() -> Unit) {
    with(drawContext.canvas.nativeCanvas) {
        val checkPoint = saveLayer(null, null)
        block()
        restoreToCount(checkPoint)
    }
}

private fun ratingItemPositions(
    itemSize: Float,
    space: Float,
    totalCount: Int,
): List<ClosedFloatingPointRange<Float>> {
    val list = mutableListOf<ClosedFloatingPointRange<Float>>()
    for (i in 0 until totalCount) {
        val start = itemSize * i + space * i
        list.add(start..start + itemSize)
    }
    return list
}

private fun getRatingFromTouchPosition(
    x: Float,
    itemIntervals: List<ClosedFloatingPointRange<Float>>,
    ratingBarDimension: Float,
    space: Float,
    totalCount: Int,
): Float {
    val ratingBarItemSize = (ratingBarDimension - space * (totalCount - 1)) / totalCount
    val ratingInterval = ratingBarItemSize + space

    var rating = 0f
    var isInInterval = false
    itemIntervals.forEachIndexed { index: Int, interval: ClosedFloatingPointRange<Float> ->
        if (interval.contains(x)) {
            rating = index.toFloat() + (x - interval.start) / ratingBarItemSize
            isInInterval = true
        }
    }

    rating =
        when {
            !isInInterval -> (1 + x / ratingInterval).toInt().coerceAtMost(totalCount).toFloat()
            else -> rating
        }

    return rating.coerceIn(0f, totalCount.toFloat())
}
