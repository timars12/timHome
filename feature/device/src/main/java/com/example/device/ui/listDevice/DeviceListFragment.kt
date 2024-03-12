package com.example.device.ui.listDevice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.trace
import com.example.device.di.InjectDaggerDependency
import com.example.device.di.InjectDaggerDependencyImpl
import com.example.device.domain.models.DeviceModel
import com.example.device.ui.listDevice.composables.DeviceListItem

const val SELECTED_DEVICE_ID = "selectedDeviceId"

internal class DeviceListFragment : Fragment(), InjectDaggerDependency by InjectDaggerDependencyImpl() {
    private val viewModel: DeviceListViewModel by viewModels { getAbstractFactory() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inject(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeTheme {
                    val deviceList by viewModel.deviceList.collectAsStateWithLifecycle()

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 90.dp),
                    ) {
                        trace("deviceList") {
                            items(items = deviceList, key = DeviceModel::id) { item ->
                                DeviceListItem(
                                    item = item,
                                    navigateToDetailScreen = {
                                        viewModel.navigateToDetailScreen(
                                            item,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
