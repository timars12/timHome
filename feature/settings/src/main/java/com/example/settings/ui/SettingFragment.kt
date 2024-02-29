package com.example.settings.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.coreComponent
import com.example.core.ui.theme.TextFieldBackgroundColor
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.settings.R
import com.example.settings.di.DaggerSettingComponent
import com.example.settings.ui.composables.SwitchWithText
import javax.inject.Inject

private const val CORNER_SHAPE_TEXT_FIELD = 20

internal class SettingFragment : Fragment() {
    @Inject
    lateinit var abstractFactory: dagger.Lazy<ViewModelFactory>
    private val viewModel: SettingViewModel by viewModels { abstractFactory.get() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerSettingComponent.factory().create(this.coreComponent()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
                    val isUseMock by viewModel.isUseMock.collectAsStateWithLifecycle()

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(top = 46.dp, bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        TextField(
                            value = ipAddress,
                            onValueChange = viewModel::onIpAddressEntered,
                            colors =
                                TextFieldDefaults.colors(
                                    focusedContainerColor = TextFieldBackgroundColor,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                ),
                            keyboardOptions =
                                remember {
                                    KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                    )
                                },
                            shape = remember { RoundedCornerShape(CORNER_SHAPE_TEXT_FIELD) },
                            maxLines = 1,
                            textStyle =
                                LocalTextStyle.current.copy(
                                    textAlign = TextAlign.Center,
                                    fontSize = 26.sp,
                                    lineHeight = 32.sp,
                                ),
                            label = { Text(text = stringResource(R.string.ip_address)) },
                        )
//                        TextField(
//                            modifier = Modifier.padding(top = 48.dp),
//                            value = "",
//                            onValueChange = { },
//                            colors =
//                                TextFieldDefaults.colors(
//                                    focusedContainerColor = TextFieldBackgroundColor,
//                                    focusedIndicatorColor = Color.Transparent,
//                                    unfocusedIndicatorColor = Color.Transparent,
//                                    disabledIndicatorColor = Color.Transparent,
//                                ),
//                            keyboardOptions =
//                                remember {
//                                    KeyboardOptions(
//                                        keyboardType = KeyboardType.Text,
//                                    )
//                                },
//                            shape = remember { RoundedCornerShape(CORNER_SHAPE_TEXT_FIELD) },
//                            maxLines = 1,
//                            textStyle =
//                                LocalTextStyle.current.copy(
//                                    textAlign = TextAlign.Center,
//                                    fontSize = 26.sp,
//                                    lineHeight = 32.sp,
//                                ),
//                            label = { Text(text = stringResource(R.string.location)) },
//                        )
                        SwitchWithText(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp, start = 64.dp, end = 48.dp),
                            text = stringResource(R.string.is_use_mock_date),
                            isUseMock = isUseMock,
                            onSetUseMockClick = viewModel::onSetUseMockClick,
                        )

                        // TODO add select from and to time when we should show notification about co2
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        bottom = 28.dp,
                                        start = 64.dp,
                                        end = 64.dp,
                                    )
                                    .height(60.dp),
                            shape = remember { RoundedCornerShape(CORNER_SHAPE_TEXT_FIELD) },
                            onClick = viewModel::onSaveChangClick,
                        ) {
                            Text(text = stringResource(R.string.save), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
