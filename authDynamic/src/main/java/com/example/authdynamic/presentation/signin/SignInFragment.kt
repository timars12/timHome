package com.example.authdynamic.presentation.signin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.core.coreComponent
import com.example.core.utils.viewmodel.InjectingSavedStateViewModelFactory
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
                    val email by viewModel.email.observeAsState()
                    val password by viewModel.password.observeAsState()
                    var passwordVisible by rememberSaveable { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = com.example.modularizationtest.R.drawable.ic_background_login),
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            TextField(
                                value = email ?: "",
                                onValueChange = viewModel::onEnterEmail,
                                singleLine = true,
                                placeholder = {
                                    Text(text = "Email")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )

                            TextField(
                                value = password ?: "",
                                onValueChange = viewModel::onEnterPassword,
                                singleLine = true,
                                placeholder = {
                                    Text(text = "Password")
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    val image = if (passwordVisible)
                                        Icons.Filled.ArrowBack
                                    else Icons.Filled.Warning

                                    // Please provide localized description for accessibility services
                                    val description =
                                        if (passwordVisible) "Hide password" else "Show password"

                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = image, description)
                                    }
                                }
                            )

                            Button(onClick = viewModel::onSignInByEmail) {
                                Text("Sign In")
                            }
                        }
                    }
                }
            }
        }
    }
}