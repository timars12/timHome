package com.timhome.soilmoisture.ui.roomedit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.timhome.core.designsystem.theme.HomeTheme
import com.timhome.core.ui.viewmodel.ViewModelFactory
import com.timhome.soilmoisture.R

@Composable
internal fun RoomEditScreen(
    abstractFactory: dagger.Lazy<ViewModelFactory>,
    viewModel: RoomEditViewModel = viewModel(factory = abstractFactory.get()),
) {
    HomeTheme {
        val state by viewModel.uiState.collectAsStateWithLifecycle()

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.name,
                onValueChange = viewModel::onNameChanged,
                label = { Text(text = stringResource(R.string.room_name)) },
                singleLine = true,
            )
            TextField(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                value = state.ipAddress,
                onValueChange = viewModel::onIpAddressChanged,
                label = { Text(text = stringResource(R.string.room_ip_address)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                onClick = viewModel::onSaveClick,
            ) {
                Text(text = stringResource(R.string.save))
            }
            if (state.isExistingRoom) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    onClick = viewModel::onDeleteClick,
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}
