package com.example.device.ui.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.ui.theme.HomeTheme
import com.example.core.ui.theme.cornerRoundedShapes
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.device.ui.device.composables.LazyColumnWithParallax

@Composable
internal fun DeviceDetailScreen(
    abstractFactory: dagger.Lazy<ViewModelFactory>,
    viewModel: DeviceDetailViewModel = viewModel(factory = abstractFactory.get()),
) {
    HomeTheme {
        val device by viewModel.device.collectAsStateWithLifecycle()
        val modules by viewModel.module.collectAsStateWithLifecycle()

        if (device == null) return@HomeTheme
        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(visible = modules.any { it.isSelectToBuy }) {
                    FloatingActionButton(
                        modifier =
                            Modifier
                                .padding(24.dp),
                        onClick = remember { viewModel::buyModules },
                        shape = MaterialTheme.cornerRoundedShapes.rounded,
                    ) {
                        Icon(
                            Icons.Rounded.ShoppingCart,
                            contentDescription = null,
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            floatingActionButtonPosition = FabPosition.End,
        ) { padding ->
            Surface(modifier = Modifier.padding(padding)) {
                LazyColumnWithParallax(
                    modifier =
                        Modifier
                            .background(color = MaterialTheme.colorScheme.background)
                            .fillMaxSize(),
                    device = device!!,
                    modules = modules,
                    onBackClick = viewModel::goBack,
                    onSelectModuleToBuy = viewModel::selectModuleToBuy,
                )
            }
        }
    }
}
