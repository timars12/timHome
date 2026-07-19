package com.timhome.soilmoisture.ui.list.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timhome.soilmoisture.R
import com.timhome.soilmoisture.ui.list.RoomUiModel

private const val CARD_CORNER_RADIUS = 16

@Composable
internal fun RoomCard(
    room: RoomUiModel,
    onWaterClick: (potId: Int) -> Unit,
    onEditRoomClick: () -> Unit,
    onEditPotClick: (potId: Int) -> Unit,
    onAddPotClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(CARD_CORNER_RADIUS),
                )
                .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = room.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onEditRoomClick) {
                Text(text = stringResource(R.string.soil_moisture_edit_room))
            }
        }
        if (room.isConnected) {
            Text(
                text = "🌡 ${room.temperature?.let { "%.1f°C".format(it) } ?: "—"}   💧 ${room.humidity?.let { "%.0f%%".format(it) } ?: "—"}",
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            Text(
                text = room.connectionError ?: stringResource(R.string.soil_moisture_no_connection),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        if (room.pots.isEmpty()) {
            Text(
                text = stringResource(R.string.soil_moisture_no_pots),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            room.pots.forEach { pot ->
                PotRow(
                    pot = pot,
                    onWaterClick = { onWaterClick(pot.id) },
                    onEditClick = { onEditPotClick(pot.id) },
                )
            }
        }
        TextButton(onClick = onAddPotClick, modifier = Modifier.align(Alignment.End)) {
            Text(text = stringResource(R.string.soil_moisture_add_pot))
        }
    }
}
