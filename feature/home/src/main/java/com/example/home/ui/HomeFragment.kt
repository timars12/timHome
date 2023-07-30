package com.example.home.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.base.DaggerBaseComponent
import com.example.core.coreComponent
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.ui.ChartView
import com.example.core.ui.theme.DataCreatedItemColor
import com.example.core.ui.theme.LightBlack
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.home.R
import com.example.home.custom.Co2Indicator
import com.example.home.custom.TemperatureBar
import com.example.home.di.DaggerHomeComponent
import com.example.home.ui.home.HomeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeFragment : Fragment() {

    @Inject
    lateinit var abstractFactory: dagger.Lazy<ViewModelFactory>
    private val viewModel: HomeViewModel by viewModels { abstractFactory.get() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerHomeComponent
            .factory()
            .create(DaggerBaseComponent.factory().create(this.coreComponent()))
            .inject(this)
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
//                    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                    val temperatureInside by viewModel.temperatureInside.collectAsStateWithLifecycle()
                    val temperatureOutside by viewModel.temperatureOutside.collectAsStateWithLifecycle()
                    val co2 by viewModel.co2.collectAsStateWithLifecycle()
                    val chartCO2 by viewModel.measureCO2Levels.collectAsStateWithLifecycle()
                    val isAnimated = remember { mutableStateOf(false) }
//                    val pullRefreshState =
//                        rememberPullRefreshState(isRefreshing, onRefresh = viewModel::getDate)

                    DisposableEffect(key1 = Lifecycle.State.RESUMED) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                                isAnimated.value = true
                            }
                        }
                        onDispose {
                            isAnimated.value = false
                        }
                    }

                    Box(
                        modifier = Modifier
//                            .pullRefresh(pullRefreshState)
                            .fillMaxSize()
                    ) {
                        Column(
                            Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .height(180.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    24.dp,
                                    alignment = Alignment.CenterHorizontally
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .fillMaxHeight()
                                ) {
                                    TemperatureBar(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        temperatureOutside,
                                        temperatureInside
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f, fill = false)
                                        .fillMaxHeight()
                                ) {
                                    Co2Indicator(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp),
                                        co2 = co2,
                                        isAnimated = isAnimated.value
                                    )
                                }
                            }
                            ChartStatisticView(chartCO2 = chartCO2)
                        }
//                        PullRefreshIndicator(
//                            isRefreshing,
//                            pullRefreshState,
//                            Modifier.align(Alignment.TopCenter)
//                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ChartStatisticView(chartCO2: ImmutableList<CarbonDioxideEntity>) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            horizontalArrangement = Arrangement.spacedBy(
                8.dp,
                alignment = Alignment.CenterHorizontally
            )
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .size(200.dp)
                    .padding(10.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(DataCreatedItemColor)
            ) {
                ChartView(
                    modifier = Modifier.fillMaxSize(),
                    xLabel = stringResource(R.string.time),
                    yLabel = "Temperature",
                    isShouldShowValue = false,
                    list = listOf(
                        // TODO FAKE
                        CarbonDioxideEntity(co2Level = 540, date = "12:00"),
                        CarbonDioxideEntity(co2Level = 600, date = "12:15"),
                        CarbonDioxideEntity(co2Level = 660, date = "12:30"),
                        CarbonDioxideEntity(co2Level = 700, date = "12:45"),
                        CarbonDioxideEntity(co2Level = 760, date = "13:00")
                    ).toImmutableList()
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f, fill = true)
                    .size(200.dp)
                    .padding(10.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(LightBlack)
            ) {
                ChartView(
                    modifier = Modifier.fillMaxSize(),
                    xLabel = stringResource(R.string.time),
                    yLabel = stringResource(R.string.co2),
                    isShouldShowValue = false,
                    list = chartCO2
                )
            }
        }
    }
}
