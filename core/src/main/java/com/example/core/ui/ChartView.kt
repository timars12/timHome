package com.example.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.ui.theme.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalTextApi::class)
@Composable
fun ChartView(
    modifier: Modifier,
    xLabel: String,
    yLabel: String,
    isShouldShowValue: Boolean,
    list: ImmutableList<CarbonDioxideEntity>?
) {
    if (list.isNullOrEmpty() || list.size < 2) return
    val textMeasure = rememberTextMeasurer()
    // Define the x and y axis labels and the chart's padding
    val padding = 50.dp

    // Determine the min and max values of x and y
    val xMin = 0f
    val xMax = list.size.toFloat()
    val yMin = 0f
    val yMax = remember(list.size) { list.maxOf { it.co2Level.toFloat() } }

    Canvas(modifier = modifier) {
        // Calculate the scale factors for x and y
        val xScale = (size.width - padding.value * 2) / (xMax - xMin)
        val yScale = (size.height - padding.value * 2) / (yMax - yMin)

        drawAxisLine(
            textMeasure = textMeasure,
            padding = padding.value,
            xLabel = xLabel,
            yLabel = yLabel
        )

        // Draw the line chart
        val path = Path()
        list.forEachIndexed { index, entity ->
            val x = padding.value + index * xScale
            val y = size.height - padding.value - (entity.co2Level.toFloat() - yMin) * yScale
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            drawCircle(color = IndicatorCO2Bed, radius = 6f, center = Offset(x, y))
            if (isShouldShowValue) {
                drawText(
                    textMeasurer = textMeasure,
                    text = entity.co2Level.toString(),
                    style = Typography.chartValueTextStyle,
                    maxLines = 1,
                    topLeft = Offset(0f, y)
                )

                drawText(
                    textMeasurer = textMeasure,
                    text = entity.date,
                    style = Typography.chartValueTextStyle,
                    maxLines = 1,
                    topLeft = Offset(x - padding.value, size.height - padding.value)
                )
            }
        }
        drawPath(path, SelectedTabBottomBar, style = Stroke(2f))
    }
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawAxisLine(
    textMeasure: TextMeasurer,
    padding: Float,
    xLabel: String,
    yLabel: String,
) {
    val textAxisOffset = 10f

    val xAxisLabel = textMeasure.measure(xLabel, maxLines = 1)
    val yAxisLabel = textMeasure.measure(yLabel, maxLines = 1)

    // Draw the x and y axes and labels
    drawLine(
        color = DeviceDetailForegroundColor,
        start = Offset(padding, size.height - padding),
        end = Offset(size.width - padding, size.height - padding)
    )
    drawLine(
        color = DeviceDetailForegroundColor,
        start = Offset(padding, padding),
        end = Offset(padding, size.height - padding)
    )
    drawText(
        textMeasurer = textMeasure,
        text = xLabel,
        style = Typography.chartAxisTextStyle,
        maxLines = 1,
        topLeft = Offset(
            size.width - padding - xAxisLabel.size.width,
            size.height - padding
        )
    )
    rotate(degrees = 270F) {
        drawText(
            textMeasurer = textMeasure,
            text = yLabel,
            style = Typography.chartAxisTextStyle,
            maxLines = 1,
            topLeft = Offset(
                x = size.width - padding - yAxisLabel.size.width + textAxisOffset,
                y = yAxisLabel.size.height.toFloat() - padding / 2
            )
        )
    }
}

@Preview
@Composable
fun TestChartView() {
    ChartView(
        modifier = Modifier.size(400.dp),
        xLabel = "Time",
        yLabel = "CO2 Level",
        isShouldShowValue = false,
        list = listOf(
            CarbonDioxideEntity(co2Level = 400, date = "12:00"),
            CarbonDioxideEntity(co2Level = 600, date = "12:15"),
            CarbonDioxideEntity(co2Level = 660, date = "12:30"),
            CarbonDioxideEntity(co2Level = 700, date = "12:45"),
            CarbonDioxideEntity(co2Level = 900, date = "13:00"),
            CarbonDioxideEntity(co2Level = 1200, date = "13:08"),
            CarbonDioxideEntity(co2Level = 1200, date = "13:15"),
            CarbonDioxideEntity(co2Level = 1400, date = "13:30"),
            CarbonDioxideEntity(co2Level = 1600, date = "13:45"),
            CarbonDioxideEntity(co2Level = 3800, date = "14:00"),
        ).toImmutableList()
    )
}
