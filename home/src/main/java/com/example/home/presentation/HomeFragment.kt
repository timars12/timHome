package com.example.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.home.custom.Co2Indicator
import com.example.home.presentation.home.HomeViewModel

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {

                    val brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colors.primaryVariant,
                            Color(0xffffc0cb)
                        )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterHorizontally)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(.5f)
                                    .background(color = Color.Blue)
                            )
                            Co2Indicator(modifier = Modifier.fillMaxSize(.5f), co2 = 302)
                        }

                    }
                }
            }
        }
    }
}