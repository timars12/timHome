package com.example.settings.ui.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun SwitchWithText(
    modifier: Modifier = Modifier,
    text: String,
    isUseMock: Boolean,
    onSetUseMockClick: (Boolean) -> Unit,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(text, color = MaterialTheme.colorScheme.onTertiary)
        Spacer(modifier = Modifier.weight(1f))
        Switch(checked = isUseMock, onCheckedChange = onSetUseMockClick)
    }
}

@Preview
@Composable
fun PreviewChooseUseMockDate() {
    SwitchWithText(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        text = "Is use mock date:",
        isUseMock = true,
        onSetUseMockClick = { },
    )
}
