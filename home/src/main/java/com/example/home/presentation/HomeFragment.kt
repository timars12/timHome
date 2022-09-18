package com.example.home.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.core.coreComponent
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.example.home.custom.Co2Indicator
import com.example.home.custom.TemperatureBar
import com.example.home.di.DaggerHomeComponent
import com.example.home.presentation.home.HomeViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var abstractFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

    /**
     * This method androidx uses for `by viewModels` method.
     * We can set out injecting factory here and therefore don't touch it again later
     */
    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory =
        abstractFactory.get().create(this, arguments)

    private val viewModel: HomeViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerHomeComponent.factory().create(this.coreComponent()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val isRefreshing by viewModel.isRefreshing.collectAsState()
                    val temperatureInside by viewModel.temperatureInside.collectAsState()
                    val temperatureOutside by viewModel.temperatureOutside.collectAsState()
                    val co2 by viewModel.co2.collectAsState()

                    SwipeRefresh(
                        modifier = Modifier.fillMaxSize(),
                        state = rememberSwipeRefreshState(isRefreshing),
                        onRefresh = viewModel::getDate,
                    ) {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(320.dp),
                                horizontalArrangement = Arrangement.spacedBy(
                                    24.dp,
                                    alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                TemperatureBar(
                                    modifier = Modifier.fillMaxSize(.5f),
                                    temperatureOutside,
                                    temperatureInside
                                )
                                Co2Indicator(modifier = Modifier.fillMaxSize(.5f), co2 = co2)
                            }
                        }
                    }
                }
            }
        }
    }
}
