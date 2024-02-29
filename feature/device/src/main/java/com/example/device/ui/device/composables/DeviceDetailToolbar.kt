package com.example.device.ui.device.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.R
import com.example.core.ui.theme.AuthTabSectionBackgroundColor
import com.example.core.utils.OnClick

@Composable
internal fun DeviceDetailToolbar(
    modifier: Modifier = Modifier,
    calculateOpacity: () -> Float,
    title: String,
    isShowTextOnToolbar: () -> Boolean,
    onBackClick: OnClick,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        TextToolbar(
            modifier =
                Modifier
                    .fillMaxSize(),
            title = title,
            calculateOpacity = calculateOpacity,
            isShowTextOnToolbar = isShowTextOnToolbar,
        )
        Image(
            modifier =
                Modifier
                    .clickable { onBackClick() }
                    .padding(start = 8.dp),
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = null,
        )
    }
}

@Composable
fun TextToolbar(
    modifier: Modifier,
    title: String,
    calculateOpacity: () -> Float,
    isShowTextOnToolbar: () -> Boolean,
) {
    Box(
        modifier =
            modifier
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                    alpha = calculateOpacity()
                }
                .background(AuthTabSectionBackgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        if (isShowTextOnToolbar()) {
            Text(
                text = title,
                style =
                    TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.W900,
                        color = Color.White,
                    ),
                maxLines = 1,
            )
        }
    }
}
