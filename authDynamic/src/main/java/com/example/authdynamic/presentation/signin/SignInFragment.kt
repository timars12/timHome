package com.example.authdynamic.presentation.signin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.dynamicfeatures.DynamicExtras
import androidx.navigation.dynamicfeatures.DynamicInstallMonitor
import androidx.navigation.fragment.findNavController
import com.example.authdynamic.R
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.authdynamic.presentation.signin.composable.EmailTextField
import com.example.authdynamic.presentation.signin.composable.PasswordTextField
import com.example.core.coreComponent
import com.example.core.ui.SnackbarMessage
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class SignInFragment : Fragment() {

    @Inject
    lateinit var abstractFactory: dagger.Lazy<InjectingSavedStateViewModelFactory>

    /**
     * This method androidx uses for `by viewModels` method.
     * We can set out injecting factory here and therefore don't touch it again later
     */
    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory =
        abstractFactory.get().create(this, arguments)

    private val viewModel: SignInViewModel by viewModels()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerAuthenticationComponent.factory().create(this.coreComponent()).inject(this)
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

                    val lifecycleOwner = LocalLifecycleOwner.current
                    val focusManager = LocalFocusManager.current
                    val snackbarHostState = remember { SnackbarHostState() }
                    val email by viewModel.email.observeAsState()
                    val password by viewModel.password.observeAsState()

                    val events = remember(viewModel.events, lifecycleOwner) {
                        viewModel.events.receiveAsFlow().flowWithLifecycle(
                            lifecycleOwner.lifecycle,
                            Lifecycle.State.STARTED
                        )
                    }

                    LaunchedEffect(Unit) {
                        events.collect { event ->
                            when (event) {
                                SignInEvent.GoToHomeScreen -> navigateToHome()
                                is SignInEvent.ShowErrorMessage -> {
                                    snackbarHostState.showSnackbar(message = event.error)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            EmailTextField(email, focusManager, viewModel::onEnterEmail)
                            PasswordTextField(
                                password = password,
                                focusManager = focusManager,
                                onEnterText = viewModel::onEnterPassword,
                                onClickDone = viewModel::onSignInByEmail
                            )
                            Button(onClick = viewModel::onSignInByEmail) {
                                Text(stringResource(id = R.string.sign_in))
                            }
                        }
                        SnackbarMessage(
                            snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .navigationBarsPadding()
                        )
                    }
                }
            }
        }
    }

    private fun navigateToHome() {
        val installMonitor = DynamicInstallMonitor()
        findNavController().navigate(
            com.example.modularizationtest.R.id.home_navigation,
            null,
            null,
            DynamicExtras(installMonitor)
        )

        if (installMonitor.isInstallRequired) {
            installMonitor.status.observe(
                viewLifecycleOwner,
                object : Observer<SplitInstallSessionState> {
                    override fun onChanged(sessionState: SplitInstallSessionState) {
                        when (sessionState.status()) {
                            SplitInstallSessionStatus.INSTALLED -> {
                                findNavController().navigate(com.example.modularizationtest.R.id.home_navigation)
                            }
                            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
//                            SplitInstallManager.startConfirmationDialogForResult(...)
                            }
                            // Handle all remaining states:
                            SplitInstallSessionStatus.FAILED -> {
                                Log.e("0707", sessionState.errorCode().toString())
                            }
                            SplitInstallSessionStatus.CANCELED -> {
                            }
                            else -> {
                            }
                        }

                        if (sessionState.hasTerminalStatus()) {
                            installMonitor.status.removeObserver(this)
                        }
                    }
                }
            )
        }
    }
}
