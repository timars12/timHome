package com.example.device.ui.buyModule

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
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.ui.theme.AuthTabSectionBackgroundColor
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.device.R
import com.example.device.data.model.ModuleModel
import com.example.device.ui.buyModule.composables.OrderInfo
import com.example.device.ui.device.composables.DeviceDetailImage
import com.example.device.ui.device.composables.ModuleItemContent
import kotlinx.collections.immutable.ImmutableList
import java.math.BigDecimal

internal const val BUY_LIST_FRACTION = .7F
internal const val BUY_SECTION_FRACTION = .3F

@Composable
@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
internal fun BuyModuleScreen(
    abstractFactory: dagger.Lazy<ViewModelFactory>,
    viewModel: BuyModuleViewModel = viewModel(factory = abstractFactory.get()),
) {
    HomeTheme {
        val totalPrice by viewModel.totalPrice.collectAsStateWithLifecycle()
        val modules by viewModel.modules.collectAsStateWithLifecycle()

        Scaffold(
            topBar = {
                TopAppBar(
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = AuthTabSectionBackgroundColor,
                            titleContentColor = MaterialTheme.colorScheme.secondary,
                        ),
                    title = {
                        Text(
                            stringResource(R.string.order_summary),
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = remember { viewModel::goBack }, colors = IconButtonDefaults.iconButtonColors().copy(contentColor = Color.Black)) {
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
                onClick = { viewModel.removeModule(it) },
                totalPrice = totalPrice,
                modules = modules,
            )
        }
    }
}

internal typealias UnSelectModule = (item: ModuleModel) -> Unit

@Composable
private fun BuyModuleContent(
    modifier: Modifier,
    onClick: UnSelectModule,
    totalPrice: BigDecimal,
    modules: ImmutableList<ModuleModel>,
) {
    val scrollState = rememberLazyListState()
    Box(modifier = modifier.fillMaxSize()) {
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
                            .clickable { onClick(item) }
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
        OrderInfo(totalPrice = totalPrice)
    }
}
