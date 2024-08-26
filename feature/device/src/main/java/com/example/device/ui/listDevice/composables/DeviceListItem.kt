package com.example.device.ui.listDevice.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.rememberAsyncImagePainter
import com.example.core.ui.theme.Black
import com.example.core.ui.theme.DataCreatedItemColor
import com.example.core.ui.theme.PriceColor
import com.example.core.utils.OnClick
import com.example.device.domain.models.DeviceModel

@Composable
@SuppressWarnings("LongMethod")
internal fun DeviceListItem(
    item: DeviceModel,
    navigateToDetailScreen: OnClick,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable { navigateToDetailScreen() },
    ) {
        ConstraintLayout(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), optimizationLevel = 4) {
            val (image, title, price, description, creationDate, rating) = createRefs()
            Image(
                modifier =
                    Modifier
                        .constrainAs(image) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            bottom.linkTo(parent.bottom)
                        }
                        .width(80.dp)
                        .height(80.dp)
                        .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop,
                painter = rememberAsyncImagePainter(item.image),
                contentDescription = null,
            )
            Text(
                modifier =
                    Modifier.constrainAs(title) {
                        top.linkTo(parent.top)
                        start.linkTo(image.end, margin = 8.dp)
                        end.linkTo(price.start)
                        width = Dimension.fillToConstraints
                    },
                text = item.title,
                maxLines = 2,
                textAlign = TextAlign.Start,
                overflow = TextOverflow.Ellipsis,
                color = Color.Black,
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                modifier =
                    Modifier.constrainAs(price) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        start.linkTo(title.end, margin = 8.dp)
                    },
                text = item.totalPrice,
                color = PriceColor,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Right,
                maxLines = 1,
            )
            if (!item.description.isNullOrBlank()) {
                Text(
                    modifier =
                        Modifier.constrainAs(description) {
                            top.linkTo(title.bottom)
                            start.linkTo(image.end, margin = 8.dp)
                            end.linkTo(parent.end)
                            width = Dimension.preferredWrapContent
                        },
                    text = item.description,
                    maxLines = 2,
                    color = Black,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                modifier =
                    Modifier.constrainAs(creationDate) {
                        top.linkTo(rating.top)
                        bottom.linkTo(rating.bottom)
                        start.linkTo(image.end, margin = 8.dp)
                    },
                text = item.dateCreated,
                fontSize = 12.sp,
                color = DataCreatedItemColor,
            )
            FavoriteWithRating(
                modifier =
                    Modifier.constrainAs(rating) {
                        top.linkTo(if (!item.description.isNullOrBlank()) description.bottom else title.bottom)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    },
                isFavorite = item.isFavorite,
                rating = item.rating,
            )
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
