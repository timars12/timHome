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

        val dimension = (size.height.coerceAtMost(size.width)).toInt()

        val outDoorTriangle = Path().let {
            it.moveTo(0f, 0f)
            it.lineTo(this.size.width - offsetBetweenTriangles.value, 0f)
            it.lineTo(0f, size.height - offsetBetweenTriangles.value)
            it.close()
            it
        }

        val inDoorTriangle = Path().let {
            it.moveTo(this.size.width, 0f + offsetBetweenTriangles.value)
            it.lineTo(this.size.width, size.height)
            it.lineTo(0f + offsetBetweenTriangles.value, size.height)
            it.close()
            it
        }

        drawPath(
            path = outDoorTriangle,
            color = Color.Transparent,
        )
        clipPath(outDoorTriangle, Intersect) {
            drawImage(
                image = outDoorBitmap.asImageBitmap(),
                dstSize = IntSize(dimension, dimension)
            )
        }

        drawContext.canvas.nativeCanvas.drawText(
            outDoorTemperature.toString(),
            size.width * .2f,
            size.height * .4f,
            paint
        )

        drawPath(
            path = inDoorTriangle,
            color = Color.Transparent,
        )
        clipPath(inDoorTriangle, Intersect) {
            drawImage(
                image = inDoorBitmap.asImageBitmap(),
                dstSize = IntSize(dimension, dimension)
            )
        }
        drawContext.canvas.nativeCanvas.drawText(
            inDoorTemperature.toString(),
            size.width * .6f,
            size.height * .7f,
            paint
        )
    }
}
