package com.example.device.ui.buyModule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.ui.theme.DeviceDetailForegroundColor
import com.example.core.ui.theme.HomeTheme
import com.example.device.R
import com.example.device.data.model.ModuleModel
import com.example.device.di.InjectDaggerDependency
import com.example.device.di.InjectDaggerDependencyImpl
import com.example.device.ui.buyModule.composables.PaymentInfo
import com.example.device.ui.device.composables.DeviceDetailImage
import com.example.device.ui.device.composables.ModuleItemContent
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

internal const val BUY_LIST_FRACTION = .65F
internal const val BUY_SECTION_FRACTION = .35F

internal class BuyModuleFragment :
    Fragment(),
    InjectDaggerDependency by InjectDaggerDependencyImpl() {
    private val viewModel: BuyModuleViewModel by viewModels { getAbstractFactory() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        inject(context)
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeTheme {
                    val totalPrice by viewModel.totalPrice.collectAsStateWithLifecycle()
                    val modules by viewModel.modules.collectAsStateWithLifecycle()

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                colors =
                                    TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color.White,
                                        titleContentColor = MaterialTheme.colorScheme.secondary,
                                    ),
                                title = {
                                    Text(stringResource(R.string.order_summary))
                                },
                                navigationIcon = {
                                    IconButton(onClick = remember { viewModel::goBack }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                        )
                                    }
                                },
                            )
                        },
                    ) { innerPadding ->
                        BuyModuleContent(
                            modifier = Modifier.padding(innerPadding),
                            totalPrice = totalPrice,
                            modules = modules,
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun BuyModuleContent(
        modifier: Modifier,
        totalPrice: BigDecimal,
        modules: ImmutableList<ModuleModel>,
    ) {
        val scrollState = rememberLazyListState()
        Box(
            modifier =
            modifier
                .fillMaxSize()
                .background(color = DeviceDetailForegroundColor),
        ) {
            LazyColumn(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(BUY_LIST_FRACTION),
                state = scrollState,
                contentPadding = PaddingValues(vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(modules, key = { item -> item.id }) { item ->
                    Row(
                        modifier =
                        Modifier
                            .clickable {
                                viewModel.removeModule(item)
                            }
                            .padding(horizontal = 8.dp)
                            .background(color = Color.White, shape = MaterialTheme.shapes.medium)
                            .heightIn(min = 70.dp)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DeviceDetailImage(item.image)
                        ModuleItemContent(item)
                    }
                }
            }
            PaymentInfo(totalPrice = totalPrice)
        }
    }
}
