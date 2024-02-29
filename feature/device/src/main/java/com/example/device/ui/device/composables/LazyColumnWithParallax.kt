package com.example.device.ui.device.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.BackgroundColor
import com.example.core.ui.theme.DeviceDetailForegroundColor
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.core.utils.OnClick
import com.example.device.R
import com.example.device.data.model.ModuleModel
import com.example.device.domain.models.DeviceModel
import kotlin.math.max
import kotlin.math.min
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun LazyColumnWithParallax(
    modifier: Modifier = Modifier,
    device: DeviceModel,
    modules: ImmutableList<ModuleModel>,
    onBackClick: OnClick,
) {
    val scrollState = rememberLazyListState()
    val isShowTextOnToolbar = remember { mutableStateOf(false) }
    val calculateOpacity =
        remember {
            derivedStateOf {
                val calculateOpacity =
                    when {
                        scrollState.firstVisibleItemScrollOffset == 0 -> 0f
                        scrollState.layoutInfo.visibleItemsInfo.isNotEmpty() && scrollState.firstVisibleItemIndex == 0 -> {
                            val imageSize = scrollState.layoutInfo.visibleItemsInfo[0].size
                            val scrollOffset = scrollState.firstVisibleItemScrollOffset + 200
                            scrollOffset / imageSize.toFloat()
                        }
                        else -> 1f
                    }

                isShowTextOnToolbar.value = calculateOpacity >= 0.96f
                calculateOpacity
            }
        }
    val density = LocalDensity.current.density
    val calculateTextSize =
        remember {
            derivedStateOf {
                val calculateTextSize =
                    scrollState.firstVisibleItemScrollOffset.toFloat() * 0.07f / density
                max(22f, min(48f, 48f - calculateTextSize)).sp
            }
        }
    val modifierItem =
        Modifier
            .fillMaxWidth()
            .height(112.dp)
            .padding(start = 16.dp, end = 16.dp, top = 8.dp)

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(bottom = 90.dp),
        ) {
            item {
                DeviceDetailHeader(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(MaterialTheme.cornerRoundedShapes.micro),
                    image = device.image,
                    title = device.title,
                    calculateTextSize = { calculateTextSize.value },
                )
            }
            item {
                HatOfList(
                    Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                )
            }
            items(modules, key = { item -> item.id }) { item ->
                DeviceDetailListItems(modifierItem, item)
            }
        }

        DeviceDetailToolbar(
            modifier =
                Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(60.dp),
            calculateOpacity = { calculateOpacity.value },
            title = device.title,
            isShowTextOnToolbar = { isShowTextOnToolbar.value },
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun HatOfList(modifier: Modifier) {
    Box(modifier = modifier.background(BackgroundColor)) {
        Box(
            modifier =
                modifier
                    .background(
                        color = DeviceDetailForegroundColor,
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                    ),
        ) {
            Text(
                modifier =
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp),
                text = stringResource(R.string.elements),
                fontSize = 16.sp,
                fontWeight = FontWeight.W700,
            )
        }
    }
}
