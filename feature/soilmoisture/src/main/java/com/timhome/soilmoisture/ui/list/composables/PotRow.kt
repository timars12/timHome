package com.timhome.soilmoisture.ui.list.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.timhome.soilmoisture.R
import com.timhome.soilmoisture.ui.list.PotUiModel

private const val PERCENT_TO_FRACTION = 100f
private const val PROGRESS_CORNER_RADIUS = 8

@Composable
internal fun PotRow(
    pot: PotUiModel,
    onWaterClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onEditClick),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = pot.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = pot.moisturePercent?.let { "$it%" } ?: stringResource(R.string.soil_moisture_moisture_unknown),
                style = MaterialTheme.typography.titleMedium,
            )
        }
        LinearProgressIndicator(
            progress = { (pot.moisturePercent ?: 0) / PERCENT_TO_FRACTION },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(PROGRESS_CORNER_RADIUS)),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    pot.lastWateredAt?.let { stringResource(R.string.soil_moisture_last_watered, it) }
                        ?: stringResource(R.string.soil_moisture_never_watered),
                style = MaterialTheme.typography.bodySmall,
            )
            Button(onClick = onWaterClick) {
                Text(text = stringResource(R.string.soil_moisture_water_now))
            }
        }
        if (pot.lastWateringEffective == false) {
            Text(
                text = stringResource(R.string.soil_moisture_watering_ineffective),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}
