package com.example.device.ui.buyModule.composables

import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.device.R
import com.example.device.ui.buyModule.BUY_SECTION_FRACTION
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
internal fun BoxScope.PaymentInfo(totalPrice: BigDecimal) {
    val animSize by animateValueAsState(
        totalPrice,
        TwoWayConverter(
            convertToVector = { value: BigDecimal ->
                AnimationVector1D(value.toFloat())
            },
            convertFromVector = { vector: AnimationVector1D ->
                return@TwoWayConverter BigDecimal(vector.value.toString()).setScale(
                    2,
                    RoundingMode.CEILING,
                )
            },
        ),
        animationSpec =
            tween(
                durationMillis = 1500,
                delayMillis = 100,
                easing = LinearOutSlowInEasing,
            ),
        label = "",
    )

    val textPrice =
        buildAnnotatedString {
            append(animSize.toPlainString())
            append("$")
        }

    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(BUY_SECTION_FRACTION)
            .align(Alignment.BottomCenter),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.Black,
        )
        Spacer(
            modifier =
                Modifier
                    .height(32.dp),
        )
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.estimated_total),
                fontSize = 24.sp,
                maxLines = 1,
            )

            Text(
                text = textPrice,
                fontWeight = FontWeight.W700,
                fontSize = 24.sp,
                maxLines = 1,
            )
        }
        Button(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 28.dp,
                    start = 16.dp,
                    end = 16.dp,
                )
                .height(60.dp),
            shape = MaterialTheme.shapes.medium,
            onClick = {},
        ) {
            Text(text = stringResource(R.string.confirm_order), fontSize = 20.sp)
        }
        Spacer(
            modifier =
                Modifier
                    .height(100.dp),
        )
    }
}
