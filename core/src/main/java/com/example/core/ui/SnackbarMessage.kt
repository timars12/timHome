package com.example.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SnackbarMessage(snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(
        hostState = snackbarHostState,
        snackbar = { data ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                contentColor = Color.Black
            ) {
                Text(
                    text = data.visuals.message,
                    color = Color.White
                )
            }
        },
        modifier = modifier
    )
}
