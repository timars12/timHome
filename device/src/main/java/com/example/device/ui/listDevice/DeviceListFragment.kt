package com.example.device.ui.listDevice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.example.core.coreComponent
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.device.di.DaggerDeviceComponent
import com.example.device.domain.models.DeviceModel
import com.example.device.ui.listDevice.composables.DeviceListItem
import javax.inject.Inject

const val SELECTED_DEVICE_ID = "selectedDeviceId"

class DeviceListFragment : Fragment() {
    @Inject
    lateinit var abstractFactory: dagger.Lazy<ViewModelFactory>
    private val viewModel: DeviceListViewModel by viewModels { abstractFactory.get() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerDeviceComponent.factory().create(this.coreComponent()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeTheme {
                    val deviceList by viewModel.deviceList.collectAsStateWithLifecycle()

                    LaunchedEffect(key1 = Unit) {
                        viewModel.getAllDevices()
                            .flowWithLifecycle(
                                viewLifecycleOwner.lifecycle,
                                Lifecycle.State.STARTED
                            )
                            .collect {
                                viewModel.updateList(it)
                            }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(items = deviceList, key = DeviceModel::id) { item ->
                            DeviceListItem(
                                item = item,
                                navigateToDetailScreen = { viewModel.navigateToDetailScreen(item) }
                            )
                        }
                    }
                }
            }
        }
    }
}
