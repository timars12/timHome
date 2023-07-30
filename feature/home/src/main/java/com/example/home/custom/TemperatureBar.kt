package com.example.home.custom

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.ClipOp.Companion.Intersect
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.home.R

private const val SIZE_WIDTH_IN_DOOR_TEMPERATURE = 0.6f
private const val SIZE_HEIGHT_IN_DOOR_TEMPERATURE = 0.7f
private const val SIZE_WIDTH_OUT_DOOR_TEMPERATURE = 0.2f
private const val SIZE_HEIGHT_OUT_DOOR_TEMPERATURE = 0.4f

@Composable
fun TemperatureBar(modifier: Modifier, outDoorTemperature: Int, inDoorTemperature: Int) {
    val offsetBetweenTriangles = remember { 16.dp }
    val fontSize = remember { 66.sp }
    val resources = LocalContext.current.resources
    val option = remember {
        BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.RGB_565
        }
    }
    val outDoorBitmap = remember {
        BitmapFactory.decodeResource(resources, R.mipmap.ic_sun, option)
    }

    val inDoorBitmap = remember {
        BitmapFactory.decodeResource(resources, R.mipmap.ic_room, option)
    }

    val paint = Paint().apply {
        textSize = fontSize.value
        textAlign = Paint.Align.CENTER
        color = Color.White.toArgb()
    }

    Canvas(
        modifier = modifier
    ) {
        val dimension = size.height.coerceAtMost(size.width).toInt()

        val outDoorTriangle = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width - offsetBetweenTriangles.value, 0f)
            lineTo(0f, size.height - offsetBetweenTriangles.value)
            close()
        }

        val inDoorTriangle = Path().apply {
            moveTo(size.width, 0f + offsetBetweenTriangles.value)
            lineTo(size.width, size.height)
            lineTo(0f + offsetBetweenTriangles.value, size.height)
            close()
        }

        drawPath(
            path = outDoorTriangle,
            color = Color.Transparent
        )
        clipPath(outDoorTriangle, Intersect) {
            drawImage(
                image = outDoorBitmap.asImageBitmap(),
                dstSize = IntSize(dimension, dimension)
            )
        }

        drawContext.canvas.nativeCanvas.drawText(
            outDoorTemperature.toString(),
            size.width * SIZE_WIDTH_OUT_DOOR_TEMPERATURE,
            size.height * SIZE_HEIGHT_OUT_DOOR_TEMPERATURE,
            paint
        )

        drawPath(
            path = inDoorTriangle,
            color = Color.Transparent
        )
        clipPath(inDoorTriangle, Intersect) {
            drawImage(
                image = inDoorBitmap.asImageBitmap(),
                dstSize = IntSize(dimension, dimension)
            )
        }
        drawContext.canvas.nativeCanvas.drawText(
            inDoorTemperature.toString(),
            size.width * SIZE_WIDTH_IN_DOOR_TEMPERATURE,
            size.height * SIZE_HEIGHT_IN_DOOR_TEMPERATURE,
            paint
        )
    }
}
