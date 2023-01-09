package com.example.device.presentation.device

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.core.coreComponent
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.example.device.di.DaggerDeviceComponent
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

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
    @Suppress("UnnecessaryParentheses") // TODO
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeTheme {
                    val title by viewModel.title.collectAsStateWithLifecycle()
                    val image by viewModel.image.collectAsStateWithLifecycle()
                    val scrollState = rememberLazyListState()
                    val setTextToToolbar = remember { mutableStateOf(false) }
                    val calculateOpacity = remember {
                        derivedStateOf {
                            val calculateOpacity =
                                (scrollState.firstVisibleItemScrollOffset.toFloat() / scrollState.layoutInfo.viewportEndOffset) * 1.56f
                            val value = if (scrollState.firstVisibleItemIndex == 0) min(
                                1f,
                                calculateOpacity
                            ) else 1f
                            setTextToToolbar.value = value >= 1f
                            value
                        }
                    }
                    val calculateTextSize = remember {
                        derivedStateOf {
                            val calculateTextSize =
                                (scrollState.firstVisibleItemScrollOffset.toFloat() / scrollState.layoutInfo.viewportEndOffset) * 0.8f
                            max(22f, min(48f, 48f * (1 - calculateTextSize)))
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            state = scrollState
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(500.dp),
                                    contentAlignment = Alignment.BottomStart
                                ) {
                                    Image(
                                        modifier = Modifier.fillMaxSize(),
                                        painter = rememberAsyncImagePainter(image),
                                        contentScale = ContentScale.Fit,
                                        contentDescription = null
                                    )
                                    Text(
                                        modifier = Modifier.padding(24.dp),
                                        text = title,
                                        color = Color.Black,
                                        fontSize = calculateTextSize.value.sp,
                                        fontWeight = FontWeight.W700
                                    )
                                }
                            }
                            items(viewModel.list) {
                                Text(
                                    text = "Text $it",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .background(Color.White),
                                    style = TextStyle(fontSize = 24.sp)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .alpha(calculateOpacity.value)
                                .fillMaxWidth()
                                .height(60.dp)
                                .background(Color.Yellow),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (setTextToToolbar.value) {
                                Text(
                                    text = title,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    style = TextStyle(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.W900,
                                        color = Color.Black
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
