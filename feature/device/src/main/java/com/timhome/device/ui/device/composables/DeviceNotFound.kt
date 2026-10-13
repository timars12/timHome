package com.timhome.device.ui.device.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.timhome.core.common.OnClick
import com.timhome.device.R

/** Shown instead of a device screen when its device does not exist (FR-025). */
@Composable
internal fun DeviceNotFound(
    onBackClick: OnClick,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            IconButton(modifier = Modifier.align(Alignment.TopStart), onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                )
            }
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.device_not_found),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
