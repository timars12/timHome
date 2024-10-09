package com.example.authdynamic.ui.signin

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.authdynamic.R
import com.example.authdynamic.ui.signin.composable.AuthTabSection
import com.example.authdynamic.ui.signin.composable.EmailTextField
import com.example.authdynamic.ui.signin.composable.PasswordTextField
import com.example.core.ui.SnackbarMessage
import com.example.core.ui.theme.AuthTabSectionBackgroundColor
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.mvi.ErrorType
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext

@Composable
internal fun SignInScreen(
    viewModel: SignInViewModel,
) {
    HomeTheme {
        val focusRequester = remember { FocusRequester() }
        val snackbarHostState = remember { SnackbarHostState() }
        val isLoginTabSelected = remember { mutableStateOf(true) }
        val tabNameList = remember { persistentListOf("Login", "Registration") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val viewState by viewModel.viewState.collectAsStateWithLifecycle()
        val intentChannel = remember { Channel<LoginViewIntent>(Channel.UNLIMITED) }
        val dispatch =
            remember {
                { intent: LoginViewIntent -> intentChannel.trySend(intent).getOrThrow() }
            }

        InitSideEffects(
            viewModel = viewModel,
            snackbarHostState = snackbarHostState,
            intentChannel = intentChannel,
        )

        Column(
            modifier = Modifier.fillMaxSize().testTag("login"),
        ) {
            AuthTabSection(
                tabNameList = tabNameList,
                selectedTab = {
                    isLoginTabSelected.value = it == tabNameList.first()
                },
            )
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .background(
                            color = AuthTabSectionBackgroundColor,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        ),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AnimatedVisibility(visible = isLoginTabSelected.value) {
                    LoginScreen(viewState, focusRequester, dispatch, keyboardController)
                }
                AnimatedVisibility(visible = !isLoginTabSelected.value) {
                    Column {
                        // TODO
                        Text(text = "This screen will be implemented later")
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            modifier = Modifier.align(Alignment.CenterHorizontally).testTag("go_home"),
                            onClick = {
                                dispatch(LoginViewIntent.SignInWithoutField)
                            },
                        ) {
                            Text(text = "Go to home page")
                        }
                    }
                }
            }
            SnackbarMessage(
                snackbarHostState,
                modifier =
                    Modifier
                        .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun LoginScreen(
    viewState: LoginViewState,
    focusRequester: FocusRequester,
    dispatch: (LoginViewIntent) -> Unit,
    keyboardController: SoftwareKeyboardController?,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmailTextField(
            modifier = Modifier.testTag("email_field"),
            data = viewState.email,
            focusRequester = focusRequester,
            onEnterText = { email -> dispatch(LoginViewIntent.EnterEmail(email)) },
        )
        PasswordTextField(
            data = viewState.password,
            focusRequester = focusRequester,
            onEnterText = { password ->
                dispatch(LoginViewIntent.EnterPassword(password))
            },
            onClickDone = { dispatch(LoginViewIntent.EmailSignIn) },
            keyboardController = keyboardController,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = viewState.isLoginBtnEnable,
            onClick = {
                keyboardController?.hide()
                dispatch(LoginViewIntent.EmailSignIn)
            },
        ) {
            if (viewState.isLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier
                            .size(24.dp)
                            .align(Alignment.CenterVertically),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.secondary,
                )
            } else {
                Text(stringResource(id = R.string.sign_in))
            }
        }
    }
}

@Composable
private fun InitSideEffects(
    viewModel: SignInViewModel,
    snackbarHostState: SnackbarHostState,
    intentChannel: Channel<LoginViewIntent>,
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val events =
        remember(viewModel.singleEvent, lifecycleOwner) {
            viewModel.singleEvent.receiveAsFlow().flowWithLifecycle(
                lifecycleOwner.lifecycle,
                Lifecycle.State.STARTED,
            )
        }

    LaunchedEffect(Unit) {
        events.collect { event ->
            when (event.error!!.type) {
                ErrorType.TOAST ->
                    snackbarHostState.showSnackbar(
                        message = event.error.errorMessage ?: "",
                    )

                else -> Unit
            }
        }
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Main.immediate) {
            intentChannel
                .receiveAsFlow()
                .onEach(viewModel::sendEvent)
                .flowWithLifecycle(
                    lifecycleOwner.lifecycle,
                    Lifecycle.State.STARTED,
                ).collect()
        }
    }
}

@Preview(name = "Light Mode")
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        viewState = LoginViewState.initial(),
        focusRequester = FocusRequester(),
        dispatch = {},
        keyboardController = null,
    )
}

@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenPreviewDark() {
    LoginScreen(
        viewState = LoginViewState.initial(),
        focusRequester = FocusRequester(),
        dispatch = {},
        keyboardController = null,
    )
}
