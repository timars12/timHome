package com.example.device.presentation.device

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.coreComponent
import com.example.core.ui.theme.DeviceDetailForegroundColor
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.example.device.di.DaggerDeviceComponent
import com.example.device.presentation.device.composables.LazyColumnWithParallax
import javax.inject.Inject

class DeviceDetailFragment : Fragment() {
    @Inject
    lateinit var abstractFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

    /**
     * This method androidx uses for `by viewModels` method.
     * We can set out injecting factory here and therefore don't touch it again later
     */
    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory =
        abstractFactory.get().create(this, arguments)

    private val viewModel: DeviceDetailViewMode by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerDeviceComponent.factory().create(this.coreComponent()).inject(this)
    }

    @OptIn(ExperimentalLifecycleComposeApi::class)
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
