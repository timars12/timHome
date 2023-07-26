package com.example.device.ui.device

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.theme.DeviceDetailForegroundColor
import com.example.core.ui.theme.HomeTheme
import com.example.device.di.InjectDaggerDependency
import com.example.device.di.InjectDaggerDependencyImpl
import com.example.device.ui.device.composables.LazyColumnWithParallax

class DeviceDetailFragment : Fragment(), InjectDaggerDependency by InjectDaggerDependencyImpl() {
    private val viewModel: DeviceDetailViewMode by viewModels { getAbstractFactory() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inject(context)
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
                    val device by viewModel.device.collectAsStateWithLifecycle()
                    val modules by viewModel.module.collectAsStateWithLifecycle()

                    if (device == null) return@HomeTheme
                    LazyColumnWithParallax(
                        modifier = Modifier
                            .background(color = DeviceDetailForegroundColor)
                            .fillMaxSize(),
                        device = device!!,
                        modules = modules,
                        viewModel::goBack
                    )
                }
            }
        }
    }
}
