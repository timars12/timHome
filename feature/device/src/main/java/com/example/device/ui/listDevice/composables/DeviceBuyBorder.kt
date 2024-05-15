package com.example.device.ui.listDevice.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.core.utils.OnClick
import com.example.device.R

private const val STROKE_WIDTH = 4f

@Composable
@SuppressWarnings("LongMethod", "MagicNumber")
fun DeviceBuyBorder(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: OnClick,
    textUnselect: String = stringResource(R.string.buy),
    textSelect: String = stringResource(R.string.added),
    content: @Composable RowScope.() -> Unit,
) {
    val textMeasurer = rememberTextMeasurer()
    val style = remember { TextStyle(fontSize = 10.sp, color = Color.Black) }
    val unselectedText = remember(isSelected) { textMeasurer.measure(textUnselect, style) }
    val selectedText = remember(isSelected) { textMeasurer.measure(textSelect, style) }
    val colorSelect = remember(isSelected) { if (isSelected) Color.Blue else Color.Red }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = Color.White,
                    shape = MaterialTheme.cornerRoundedShapes.small,
                )
                .drawBehind {
                    drawPath(
                        Path().apply {
                            moveTo(size.width.times(.78f), size.height.times(-0.1f))
                            cubicTo(
                                x1 = size.width.times(.78f),
                                y1 = size.height.times(-0.1f),
                                x2 = size.width.times(.75f),
                                y2 = size.height.times(-0.1f),
                                x3 = size.width.times(.75f),
                                y3 = 0f,
                            )
                            cubicTo(
                                x1 = size.width.times(.75f),
                                y1 = 0f,
                                x2 = size.width.times(.75f),
                                y2 = size.height.times(.1f),
                                x3 = size.width.times(.78f),
                                y3 = size.height.times(.1f),
                            )
                            lineTo(size.width.times(.88f), size.height.times(.1f))
                            cubicTo(
                                x1 = size.width.times(.88f),
                                y1 = size.height.times(.1f),
                                x2 = size.width.times(.9f),
                                y2 = size.height.times(.1f),
                                x3 = size.width.times(.9f),
                                y3 = size.height.times(0f),
                            )
                            cubicTo(
                                x1 = size.width.times(.9f),
                                y1 = 0f,
                                x2 = size.width.times(.9f),
                                y2 = size.height.times(-0.1f),
                                x3 = size.width.times(.87f),
                                y3 = size.height.times(-0.1f),
                            )
                            close()
                        },
                        style = Fill,
                        color = Color.White,
                    )
                    drawPath(
                        Path().apply {
                            moveTo(size.width.times(.78f), size.height.times(-0.1f))
                            cubicTo(
                                x1 = size.width.times(.78f),
                                y1 = size.height.times(-0.1f),
                                x2 = size.width.times(.75f),
                                y2 = size.height.times(-0.1f),
                                x3 = size.width.times(.75f),
                                y3 = 0f,
                            )
                            cubicTo(
                                x1 = size.width.times(.75f),
                                y1 = 0f,
                                x2 = size.width.times(.75f),
                                y2 = size.height.times(.1f),
                                x3 = size.width.times(.78f),
                                y3 = size.height.times(.1f),
                            )
                            lineTo(size.width.times(.88f), size.height.times(.1f))
                            cubicTo(
                                x1 = size.width.times(.88f),
                                y1 = size.height.times(.1f),
                                x2 = size.width.times(.9f),
                                y2 = size.height.times(.1f),
                                x3 = size.width.times(.9f),
                                y3 = size.height.times(0f),
                            )
                            cubicTo(
                                x1 = size.width.times(.9f),
                                y1 = 0f,
                                x2 = size.width.times(.9f),
                                y2 = size.height.times(-0.1f),
                                x3 = size.width.times(.87f),
                                y3 = size.height.times(-0.1f),
                            )
                            close()
                        },
                        style =
                            if (isSelected) {
                                Fill
                            } else {
                                Stroke(
                                    width = STROKE_WIDTH,
                                    cap = StrokeCap.Round,
                                )
                            },
                        color = colorSelect,
                    )

                    drawPath(
                        Path().apply {
                            moveTo(size.width.times(.9f), 0f)
                            lineTo(size.width.times(.97f), 0f)
                            cubicTo(
                                x1 = size.width.times(.97f),
                                y1 = 0f,
                                x2 = size.width,
                                y2 = 0f,
                                x3 = size.width,
                                y3 = size.height.times(.1f),
                            )
                            lineTo(size.width, size.height.times(.9f))
                            cubicTo(
                                x1 = size.width,
                                y1 = size.height.times(.9f),
                                x2 = size.width,
                                y2 = size.height,
                                x3 = size.width.times(.97f),
                                y3 = size.height,
                            )
                            lineTo(size.width.times(.03f), size.height)
                            cubicTo(
                                x1 = size.width.times(.03f),
                                y1 = size.height,
                                x2 = 0f,
                                y2 = size.height,
                                x3 = 0f,
                                y3 = size.height.times(.9f),
                            )
                            lineTo(0f, size.height.times(.1f))
                            cubicTo(
                                x1 = 0f,
                                y1 = size.height.times(.1f),
                                x2 = 0f,
                                y2 = 0f,
                                x3 = size.width.times(.03f),
                                y3 = 0f,
                            )
                            lineTo(size.width.times(.75f), 0f)
                        },
                        style = Stroke(width = STROKE_WIDTH, cap = StrokeCap.Round),
                        color = colorSelect,
                    )

                    val textOffset = if (isSelected) selectedText else unselectedText
                    drawText(
                        textLayoutResult = textOffset,
                        color = if (isSelected) Color.White else Color.Black,
                        topLeft =
                            Offset(
                                x = size.width.times(.825f) - textOffset.size.width.div(2),
                                y = 0f - textOffset.size.height.div(2),
                            ),
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        content()
    }
}

@Preview
@Composable
fun PreviewDeviceBuyBorder() {
    val isSelected =
        remember {
            mutableStateOf(false)
        }

    DeviceBuyBorder(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        isSelected = isSelected.value,
        onClick = {
            isSelected.value = !isSelected.value
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier =
                    Modifier
                        .size(74.dp)
                        .clip(shape = MaterialTheme.cornerRoundedShapes.micro)
                        .background(color = Color.Gray),
            )
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "title",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W700,
                )
            }
            Text(
                text = "price",
                color = Color.Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.W700,
            )
        }
    }
}
