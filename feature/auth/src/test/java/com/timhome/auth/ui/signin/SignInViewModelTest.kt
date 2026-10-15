package com.timhome.auth.ui.signin

import androidx.lifecycle.SavedStateHandle
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.core.common.mvi.ErrorType
import com.timhome.core.common.mvi.MviError
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.ReplaceAll
import com.timhome.core.common.navigation.SignIn
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.IOException

// Robolectric: the ViewModel logs a failed markSignedIn() with android.util.Log.
@RunWith(RobolectricTestRunner::class)
class SignInViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository: IAuthorizationRepository = mockk()
    private val firebaseAnalytics: FirebaseAnalytics = mockk(relaxed = true)
    private val sessionRepository = FakeSessionRepository()
    private val channelNavigator = ChannelNavigator()
    private val savedStateHandle = SavedStateHandle()
    private val goHome = IssuedCommand(ReplaceAll(Home), origin = SignIn)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        SignInViewModel(repository, firebaseAnalytics, sessionRepository, channelNavigator.issuedFrom(SignIn), savedStateHandle)

    /** Collects every command the ViewModel issues, for as long as the test runs. */
    private fun TestScope.issuedCommands(): List<IssuedCommand> =
        mutableListOf<IssuedCommand>().also { issued ->
            backgroundScope.launch { channelNavigator.commands.collect { issued += it } }
        }

    /** advanceUntilIdle() stops once only background work is left, so the command collector needs runCurrent(). */
    private fun settle() {
        dispatcher.scheduler.advanceUntilIdle()
        dispatcher.scheduler.runCurrent()
    }

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

            assertEquals(goHome, channelNavigator.commands.first())

            job.cancel()
        }

    @Test
    fun `successful login marks the user signed in before it goes home`() =
        runTest {
            val gate = CompletableDeferred<Unit>().also { sessionRepository.gate = it }
            val issued = issuedCommands()
            val viewModel = createViewModel()
            val job = launch { viewModel.viewState.collect {} }

            viewModel.sendEvent(LoginViewIntent.SignInWithoutField)
            settle()

            assertEquals(1, sessionRepository.markSignedInCalls)
            assertTrue(issued.isEmpty())

            gate.complete(Unit)
            settle()

            assertTrue(sessionRepository.isSignedIn.first())
            assertEquals(listOf(goHome), issued)

            job.cancel()
        }

    @Test
    fun `failed login neither marks the user signed in nor goes home`() =
        runTest {
            val failed =
                LoginViewState.initial().copy(error = MviError(type = ErrorType.FIELD, errorMessage = "Wrong password"))
            every { repository.loginByEmail(any(), any(), any()) } returns flowOf(failed)
            val issued = issuedCommands()
            val viewModel = createViewModel()
            val job = launch { viewModel.viewState.collect {} }

            viewModel.sendEvent(LoginViewIntent.EnterEmail("test@email.com"))
            viewModel.sendEvent(LoginViewIntent.EnterPassword("Password1!"))
            settle()
            viewModel.sendEvent(LoginViewIntent.EmailSignIn)
            settle()

            assertEquals(failed, viewModel.viewState.value)
            assertEquals(0, sessionRepository.markSignedInCalls)
            assertFalse(sessionRepository.isSignedIn.first())
            assertTrue(issued.isEmpty())

            job.cancel()
        }

    @Test
    fun `success state emitted again still marks signed in once and goes home once`() =
        runTest {
            val issued = issuedCommands()
            val viewModel = createViewModel()
            val firstCollector = launch { viewModel.viewState.collect {} }
            viewModel.sendEvent(LoginViewIntent.SignInWithoutField)
            settle()
            firstCollector.cancel()
            settle()

            // A new subscriber restarts the upstream, which replays the success state.
            val secondCollector = launch { viewModel.viewState.collect {} }
            settle()

            assertEquals(1, sessionRepository.markSignedInCalls)
            assertEquals(listOf(goHome), issued)

            secondCollector.cancel()
        }

    @Test
    fun `S5 a failed markSignedIn still goes home`() =
        runTest {
            sessionRepository.failure = IOException("disk full")
            val issued = issuedCommands()
            val viewModel = createViewModel()
            val job = launch { viewModel.viewState.collect {} }

            viewModel.sendEvent(LoginViewIntent.SignInWithoutField)
            settle()

            assertEquals(1, sessionRepository.markSignedInCalls)
            assertFalse(sessionRepository.isSignedIn.first())
            assertEquals(listOf(goHome), issued)

            job.cancel()
        }
}
