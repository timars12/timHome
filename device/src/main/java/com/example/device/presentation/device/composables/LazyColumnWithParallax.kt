package com.example.device.presentation.device.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.DeviceDetailForegroundColor
import com.example.core.ui.theme.PrimaryColor
import com.example.core.utils.OnClick
import com.example.device.R
import com.example.device.data.model.ModuleModel
import com.example.device.domain.models.DeviceModel
import kotlin.math.max
import kotlin.math.min

@Composable
fun LazyColumnWithParallax(
    modifier: Modifier = Modifier,
    device: DeviceModel,
    modules: List<ModuleModel>, // TODO use immutable list
    onBackClick: OnClick
) {
    val scrollState = rememberLazyListState()
    val isShowTextOnToolbar = remember { mutableStateOf(false) }
    val calculateOpacity = remember {
        derivedStateOf {
            val calculateOpacity =
                scrollState.firstVisibleItemScrollOffset.toFloat() / scrollState.layoutInfo.viewportEndOffset * 1.56f
            val value = if (scrollState.firstVisibleItemIndex == 0) min(
                1f,
                calculateOpacity
            ) else 1f
            isShowTextOnToolbar.value = value >= 0.96f
            value
        }
    }
    val calculateTextSize = remember {
        derivedStateOf {
            val calculateTextSize =
                scrollState.firstVisibleItemScrollOffset.toFloat() / scrollState.layoutInfo.viewportEndOffset * 0.8f
            max(22f, min(48f, 48f * (1 - calculateTextSize))).sp
        }
    }
    val modifierItem = Modifier
        .fillMaxWidth()
        .height(112.dp)
        .padding(start = 16.dp, end = 16.dp, top = 8.dp)

    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = scrollState,
        ) {
            item {
                DeviceDetailHeader(
                    device.image,
                    device.title,
                    calculateTextSize = { calculateTextSize.value }
                )
            }
            item {
                HatOfList(
                    Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                )
            }
            items(modules, key = { item -> item.id }) { item ->
                DeviceDetailListItems(modifierItem, item)
            }
        }

        DeviceDetailToolbar(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .height(60.dp),
            calculateOpacity = { calculateOpacity.value },
            title = device.title,
            isShowTextOnToolbar = { isShowTextOnToolbar.value },
            onBackClick = onBackClick
        )
    }
}

@Composable
fun HatOfList(modifier: Modifier) {
    Box(modifier = modifier.background(PrimaryColor)) {
        Box(
            modifier = modifier
                .background(
                    color = DeviceDetailForegroundColor,
                    shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
                )
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                text = stringResource(R.string.elements),
                fontSize = 16.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}
