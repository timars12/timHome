package com.timhome.auth.ui.signin

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.core.common.NavigationDispatcher
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SignInViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IAuthorizationRepository = mockk()
    private val firebaseAnalytics: FirebaseAnalytics = mockk()
    private val navigationDispatcher = NavigationDispatcher()
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
        SignInViewModel(repository, firebaseAnalytics, navigationDispatcher, savedStateHandle)

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

            val command = navigationDispatcher.navigationEmitter.tryReceive().getOrNull()
            assertNotNull(command)

            val navController = mockk<NavController>(relaxed = true)
            command!!.invoke(navController)
            verify { navController.navigate(any<Any>(), any<NavOptionsBuilder.() -> Unit>()) }

            job.cancel()
        }
}
