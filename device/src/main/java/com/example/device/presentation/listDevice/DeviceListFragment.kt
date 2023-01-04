package com.example.device.presentation.listDevice

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
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
import com.example.core.ui.RatingView
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.example.device.data.repository.DeviceModel
import com.example.device.di.DaggerDeviceComponent
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
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(items = deviceList, key = DeviceModel::id) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = CenterVertically
                            ) {
                                Image(
                                    modifier = Modifier
                                        .weight(2f)
                                        .height(80.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop,
                                    painter = rememberAsyncImagePainter(item.image),
                                    contentDescription = null
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(8f)
                                        .padding(start = 16.dp)
                                ) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            modifier = Modifier
                                                .weight(8f)
                                                .padding(end = 16.dp),
                                            text = item.title,
                                            maxLines = 2,
                                            overflow = Ellipsis,
                                            style = MaterialTheme.typography.h5.copy(fontSize = 18.sp)
                                        )
                                        Text(
                                            modifier = Modifier.weight(2f),
                                            text = item.totalPrice.toString(),
                                            color = Color(0xff006687),
                                            style = MaterialTheme.typography.h6,
                                            textAlign = TextAlign.Right
                                        )
                                    }
                                    if (!item.description.isNullOrBlank()) {
                                        ExpandableText(text = item.description)
                                    }
                                    FavoriteWithRating(item.isFavorite, item.rating)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpandableText(modifier: Modifier = Modifier, text: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    var isClickable by remember { mutableStateOf(false) }
    var finalText by remember { mutableStateOf(text) }

    val textLayoutResult = textLayoutResultState.value
    LaunchedEffect(textLayoutResult) {
        if (textLayoutResult == null) return@LaunchedEffect
        when {
            isExpanded -> finalText = text
            textLayoutResult.hasVisualOverflow -> {
                val lastCharIndex = textLayoutResult.getLineEnd(2 - 1)
                val showMoreString = "... Show More"
                val adjustedText = text
                    .substring(startIndex = 0, endIndex = lastCharIndex)
                    .dropLast(showMoreString.length)
                    .dropLastWhile { it == ' ' || it == '.' }

                finalText = "$adjustedText$showMoreString"
                isClickable = true
            }
        }
    }

    Text(
        text = finalText,
        style = MaterialTheme.typography.subtitle1,
        color = Color.Black,
        maxLines = if (isExpanded) Int.MAX_VALUE else 2,
        onTextLayout = { textLayoutResultState.value = it },
        modifier = modifier
            .clickable(enabled = isClickable) { isExpanded = !isExpanded }
            .animateContentSize(),
    )
}

@Composable
private fun FavoriteWithRating(isFavorite: Boolean, rating: Float) {
    Row(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = Color.DarkGray,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon = remember(isFavorite) {
            when {
                isFavorite -> com.example.core.R.drawable.ic_selected_heart
                else -> com.example.core.R.drawable.ic_unselected_heart
            }
        }
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Canvas(modifier = Modifier.size(height = 18.dp, width = 9.dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            drawLine(
                start = Offset(x = canvasWidth, y = 0f),
                end = Offset(x = 0f, y = canvasHeight),
                color = Color.Black,
                strokeWidth = 5F
            )
        }
        RatingView(
            rating = rating / 10f,
            space = 2.dp,
            imageEmpty = ImageBitmap.imageResource(id = com.example.core.R.mipmap.ic_star),
            imageFilled = ImageBitmap.imageResource(id = com.example.core.R.mipmap.ic_star_filled),
            animationEnabled = false,
            gestureEnabled = false,
            itemSize = 24.dp,
            itemCount = 1
        )
        Text(text = rating.toString())
    }
}
