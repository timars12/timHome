package com.example.device.presentation.listDevice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.core.coreComponent
import com.example.core.ui.ExpandableText
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.example.device.data.models.DeviceModel
import com.example.device.di.DaggerDeviceComponent
import com.example.device.presentation.listDevice.composables.FavoriteWithRating
import javax.inject.Inject

class DeviceListFragment : Fragment() {
    @Inject
    lateinit var abstractFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

    /**
     * This method androidx uses for `by viewModels` method.
     * We can set out injecting factory here and therefore don't touch it again later
     */
    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory =
        abstractFactory.get().create(this, arguments)

    private val viewModel: DeviceListViewModel by viewModels()

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
                    val deviceList by viewModel.deviceList.collectAsStateWithLifecycle()
                    Box(modifier = Modifier.fillMaxSize()) {

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(items = deviceList, key = DeviceModel::id) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = Color.White,
                                            shape = RoundedCornerShape(10)
                                        )
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = CenterVertically
                                ) {
                                    Image(
                                        modifier = Modifier
                                            .weight(2f)
                                            .height(80.dp)
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Crop,
                                        painter = rememberAsyncImagePainter(item.image),
                                        contentDescription = null
                                    )
                                    Column(
                                        modifier = Modifier
                                            .weight(8f)
                                            .padding(start = 16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .padding(end = 8.dp),
                                                text = item.title,
                                                maxLines = 2,
                                                textAlign = TextAlign.Start,
                                                overflow = Ellipsis,
                                                style = MaterialTheme.typography.h5
                                            )
                                            Text(
                                                text = item.totalPrice,
                                                color = Color(0xff336AD4),
                                                style = MaterialTheme.typography.h5,
                                                textAlign = TextAlign.Right,
                                                maxLines = 1
                                            )
                                        }
                                        if (!item.description.isNullOrBlank()) {
                                            ExpandableText(text = item.description)
                                        }
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 8.dp),
                                            verticalAlignment = CenterVertically
                                        ) {
                                            Text(
                                                text = item.dateCreated,
                                                fontSize = 12.sp,
                                                color = Color(0xff006687)
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            FavoriteWithRating(
                                                modifier = Modifier.padding(start = 8.dp),
                                                isFavorite = item.isFavorite,
                                                rating = item.rating
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
