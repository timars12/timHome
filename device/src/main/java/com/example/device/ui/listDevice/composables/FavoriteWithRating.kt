package com.example.device.ui.listDevice.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.core.R
import com.example.core.ui.RatingView

private const val DIVIDER_FOR_RATING = 10f

@Composable
fun FavoriteWithRating(modifier: Modifier = Modifier, isFavorite: Boolean, rating: Float) {
    Row(
        modifier = modifier
            .border(
                width = 2.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = remember(isFavorite) {
            when {
                isFavorite -> R.drawable.ic_selected_heart
                else -> R.drawable.ic_unselected_heart
            }
        }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Canvas(modifier = Modifier.size(height = 18.dp, width = 9.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            drawLine(
                start = Offset(x = canvasWidth, y = 0f),
                end = Offset(x = 0f, y = canvasHeight),
                color = Color.Black,
                strokeWidth = 5F
            )
        }
        RatingView(
            rating = rating / DIVIDER_FOR_RATING,
            space = 2.dp,
            imageEmpty = ImageBitmap.imageResource(id = R.mipmap.ic_star),
            imageFilled = ImageBitmap.imageResource(id = R.mipmap.ic_star_filled),
            animationEnabled = false,
            gestureEnabled = false,
            itemSize = 20.dp,
            itemCount = 1
        )
        Text(text = rating.toString())
    }
}
