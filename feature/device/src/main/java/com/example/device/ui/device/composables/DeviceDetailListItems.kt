package com.example.device.ui.device.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.core.ui.LinkText
import com.example.core.ui.theme.DeviceDetailItemBackgroundColor
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.device.data.model.ModuleModel

@Composable
internal fun DeviceDetailListItems(
    modifier: Modifier,
    item: ModuleModel,
) {
    Row(
        modifier =
            modifier
                .background(
                    color = DeviceDetailItemBackgroundColor,
                    shape = MaterialTheme.cornerRoundedShapes.medium,
                )
                .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            modifier =
                Modifier
                    .width(74.dp)
                    .clip(shape = MaterialTheme.cornerRoundedShapes.micro),
            painter = rememberAsyncImagePainter(model = item.image),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
        ) {
            Text(
                text = item.title,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
            )
            LinkText(text = item.link)
        }
        Text(
            text = item.price,
            color = Color.Blue,
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
        )
    }
}

@Preview
@Composable
fun Test() {
    DeviceDetailListItems(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(112.dp)
                .padding(16.dp),
        ModuleModel(
            id = 0,
            image = "https://ae01.alicdn.com/kf/H12e6cf8d37394657879c1b62b49aaa52Y/1Set-UNO-R3-Official-Box-ATMEGA16U2-UNO-WiFi-R3-MEGA328P-Chip-CH340G-For-Arduino-UNO-R3.jpg_640x640.jpg",
            title = "UNO R3 ATMEGA328P",
            price = "3.22$",
            link = "https://www.aliexpress.com/item/1005002997846504.html?spm=a2g0o.productlist.main.83.1d2fc095KXRjER&algo_pvid=e0e5cf40-9f1a-4eba-803c-a6dd250a813b&algo_exp_id=e0e5cf40-9f1a-4eba-803c-a6dd250a813b-41&pdp_ext_f=%7B\"sku_id\"%3A\"12000023136335077\"%7D&pdp_npi=2%40dis%21USD%214.0%213.56%21%21%21%21%21%402100b20d16733079013583900d06ef%2112000023136335077%21sea&curPageLogUid=LRcHs0bQN1qI",
        ),
    )
}
