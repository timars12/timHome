package com.example.device.ui.device.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.core.R
import com.example.core.ui.theme.AuthTabSectionBackgroundColor
import com.example.core.utils.OnClick

@Composable
internal fun DeviceDetailToolbar(
    modifier: Modifier = Modifier,
    scrollState: () -> LazyListState,
    title: String,
    onBackClick: OnClick,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        TextToolbar(
            modifier =
                Modifier
                    .fillMaxSize(),
            title = title,
            scrollState = scrollState,
        )
        Image(
            modifier =
                Modifier
                    .clickable { onBackClick() }
                    .padding(start = 16.dp),
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = null,
        )
    }
}

@Composable
fun TextToolbar(
    modifier: Modifier,
    title: String,
    scrollState: () -> LazyListState,
) {
    val isShowTextOnToolbar = remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    val calculatedOpacity =
                        derivedStateOf {
                            val calculateOpacity =
                                when {
                                    scrollState().firstVisibleItemScrollOffset == 0 -> 0f
                                    scrollState().layoutInfo.visibleItemsInfo.isNotEmpty() && scrollState().firstVisibleItemIndex == 0 -> {
                                        val imageSize = scrollState().layoutInfo.visibleItemsInfo[0].size
                                        val scrollOffset = scrollState().firstVisibleItemScrollOffset + 200
                                        scrollOffset / imageSize.toFloat()
                                    }

                                    else -> 1f
                                }

                            isShowTextOnToolbar.value = calculateOpacity >= 0.96f
                            calculateOpacity
                        }
                    compositingStrategy = CompositingStrategy.ModulateAlpha
                    alpha = calculatedOpacity.value
                }
                .background(AuthTabSectionBackgroundColor),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (isShowTextOnToolbar.value) {
            Text(
                modifier = Modifier.padding(start = 58.dp),
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
        }
    }
}
