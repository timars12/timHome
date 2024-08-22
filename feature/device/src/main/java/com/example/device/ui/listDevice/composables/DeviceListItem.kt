package com.example.device.ui.listDevice.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.core.ui.theme.Black
import com.example.core.ui.theme.DataCreatedItemColor
import com.example.core.ui.theme.PriceColor
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.core.utils.OnClick
import com.example.device.domain.models.DeviceModel

private const val WEIGHT_IMAGE_DEVICE_LIST = 2f
private const val WEIGHT_CONTENT_DEVICE_LIST = 8f

@Composable
internal fun DeviceListItem(
    item: DeviceModel,
    navigateToDetailScreen: OnClick,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = MaterialTheme.cornerRoundedShapes.small,
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable { navigateToDetailScreen() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier =
                Modifier
                    .weight(WEIGHT_IMAGE_DEVICE_LIST)
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.small),
            contentScale = ContentScale.Crop,
            painter = rememberAsyncImagePainter(item.image),
            contentDescription = null,
        )
        Column(
            modifier =
                Modifier
                    .weight(WEIGHT_CONTENT_DEVICE_LIST)
                    .padding(start = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                    text = item.title,
                    maxLines = 2,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = item.totalPrice,
                    color = PriceColor,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Right,
                    maxLines = 1,
                )
            }
            if (!item.description.isNullOrBlank()) {
                Text(
                    text = item.description,
                    maxLines = 2,
                    color = Black,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = item.dateCreated,
                    fontSize = 12.sp,
                    color = DataCreatedItemColor,
                )
                Spacer(modifier = Modifier.weight(1f))
                FavoriteWithRating(
                    modifier = Modifier.padding(start = 8.dp),
                    isFavorite = item.isFavorite,
                    rating = item.rating,
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewDeviceListItem() {
    Column(
        Modifier
            .background(color = Color.Blue)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DeviceListItem(
            item =
                DeviceModel(
                    id = 0,
                    image = 0,
                    title = "Test Title",
                    description = "Test Description",
                    totalPrice = "12.2",
                    dateCreated = "24.12.22",
                ),
        ) {}
        DeviceListItem(
            item =
                DeviceModel(
                    id = 0,
                    image = 0,
                    title = "Test Title",
                    description = "Test Description",
                    totalPrice = "12.2",
                    dateCreated = "24.12.22",
                ),
        ) {}
    }
}
