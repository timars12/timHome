package com.example.settings.presentation

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.coreComponent
import com.example.core.ui.theme.TextFieldBackgroundColor
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.example.settings.R
import com.example.settings.di.DaggerSettingComponent
import com.example.settings.presentation.composables.SwitchWithText
import javax.inject.Inject

private const val CORNER_SHAPE_TEXT_FIELD = 20

class SettingFragment : Fragment() {
    @Inject
    lateinit var abstractFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

    /**
     * This method androidx uses for `by viewModels` method.
     * We can set out injecting factory here and therefore don't touch it again later
     */
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() = abstractFactory.get().create(this, arguments)

    private val viewModel: SettingViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerSettingComponent.factory().create(this.coreComponent()).inject(this)
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
                    val ipAddress by viewModel.ipAddress.collectAsStateWithLifecycle()
                    val isUseMock by viewModel.isUseMock.collectAsStateWithLifecycle()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 46.dp, bottom = 80.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextField(
                            value = ipAddress,
                            onValueChange = viewModel::onIpAddressEntered,
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = TextFieldBackgroundColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Decimal) },
                            shape = remember { RoundedCornerShape(CORNER_SHAPE_TEXT_FIELD) },
                            maxLines = 1,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 26.sp,
                                lineHeight = 32.sp
                            ),
                            label = { Text(text = stringResource(R.string.ip_address)) }
                        )
                        TextField(
                            modifier = Modifier.padding(top = 48.dp),
                            value = "", // TODO
                            onValueChange = { }, // TODO
                            colors = TextFieldDefaults.textFieldColors(
                                backgroundColor = TextFieldBackgroundColor,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = remember { KeyboardOptions(keyboardType = KeyboardType.Text) },
                            shape = remember { RoundedCornerShape(CORNER_SHAPE_TEXT_FIELD) },
                            maxLines = 1,
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Center,
                                fontSize = 26.sp,
                                lineHeight = 32.sp
                            ),
                            label = { Text(text = stringResource(R.string.location)) }
                        )
                        SwitchWithText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp, start = 64.dp, end = 48.dp),
                            text = stringResource(R.string.is_use_mock_date),
                            isUseMock = isUseMock,
                            onSetUseMockClick = viewModel::onSetUseMockClick
                        )

                        // TODO add select from and to time when we should show notification about co2
                        Spacer(modifier = Modifier.weight(1f))
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    bottom = 28.dp,
                                    start = 64.dp,
                                    end = 64.dp
                                )
                                .height(60.dp),
                            shape = remember { RoundedCornerShape(CORNER_SHAPE_TEXT_FIELD) },
                            onClick = viewModel::onSaveChangClick
                        ) {
                            Text(text = stringResource(R.string.save), fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
