package com.timhome.auth.ui.signin

import androidx.lifecycle.SavedStateHandle
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.ReplaceAll
import com.timhome.core.common.navigation.SignIn
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SignInViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IAuthorizationRepository = mockk()
    private val firebaseAnalytics: FirebaseAnalytics = mockk()
    private val channelNavigator = ChannelNavigator()
    private val savedStateHandle = SavedStateHandle()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SignInViewModel(repository, firebaseAnalytics, channelNavigator.issuedFrom(SignIn), savedStateHandle)

    @Test
    fun `viewState is persisted to savedStateHandle when login is not successful`() =
        runTest {
            val viewModel = createViewModel()
            val job = launch { viewModel.viewState.collect {} }

            viewModel.sendEvent(LoginViewIntent.EnterEmail("test@email.com"))
            dispatcher.scheduler.advanceUntilIdle()

            val currentState = viewModel.viewState.value
            assertEquals("test@email.com", currentState.email.data)
            assertEquals(false, currentState.isLoginSuccess)
            assertEquals(currentState, savedStateHandle.get<LoginViewState>(LOGIN_VIEW_STATE))

            job.cancel()
        }

    @Test
    fun `viewState triggers navigation to home when login is successful`() =
        runTest {
            val viewModel = createViewModel()
            val job = launch { viewModel.viewState.collect {} }

            viewModel.sendEvent(LoginViewIntent.SignInWithoutField)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(true, viewModel.viewState.value.isLoginSuccess)

            assertEquals(IssuedCommand(ReplaceAll(Home), origin = SignIn), channelNavigator.commands.first())

            job.cancel()
        }
}
