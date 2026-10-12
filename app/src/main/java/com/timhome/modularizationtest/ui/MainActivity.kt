package com.timhome.modularizationtest.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import com.timhome.auth.ui.signin.navigation.signInEntries
import com.timhome.base.DaggerBaseComponent
import com.timhome.core.coreComponent
import com.timhome.core.designsystem.theme.HomeTheme
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.TopLevelRoute
import com.timhome.device.ui.navigation.deviceEntries
import com.timhome.home.ui.navigation.homeEntries
import com.timhome.modularizationtest.R
import com.timhome.modularizationtest.di.DaggerAppComponent
import com.timhome.modularizationtest.ui.navigation.NavigationHost
import com.timhome.modularizationtest.ui.navigation.NavigationReducer
import com.timhome.modularizationtest.ui.navigation.NavigationState
import com.timhome.modularizationtest.ui.navigation.NavigationViewModel
import com.timhome.modularizationtest.ui.navigation.topLevelTabs
import com.timhome.settings.ui.navigation.settingEntries
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var channelNavigator: ChannelNavigator

    private val navigationViewModel: NavigationViewModel by viewModels { NavigationViewModel.Factory }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        DaggerAppComponent
            .factory()
            .create(baseComponent = DaggerBaseComponent.factory().create(coreComponent()))
            .inject(this)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by navigationViewModel.state.collectAsStateWithLifecycle()
            val isShowBottomMenu = state?.isBottomBarVisible == true
            val navigationEntryProvider =
                remember {
                    entryProvider<Route> {
                        signInEntries()
                        homeEntries()
                        deviceEntries()
                        settingEntries()
                    }
                }
            val shouldShowDialog = remember { mutableStateOf(false) }

            val requestPermissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    when {
                        isGranted -> createNotificationChannel()
                        else -> shouldShowDialog.value = true
                    }
                }

            CheckNotificationPermission(requestPermissionLauncher)
            LaunchedEffect(state == null) {
                // T041 replaces this with the remembered signed-in flag.
                if (state == null) navigationViewModel.start(NavigationReducer.initialState(isSignedIn = false))
            }

            HomeTheme {
                Scaffold(
                    modifier =
                        Modifier
                            .navigationBarsPadding()
                            .fillMaxSize()
                            .semantics {
                                testTagsAsResourceId = true
                            },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        AnimatedVisibility(
                            visible = isShowBottomMenu,
                            enter =
                                slideInVertically(
                                    initialOffsetY = { y -> y },
                                    animationSpec =
                                        tween(
                                            durationMillis = 400,
                                            delayMillis = 100,
                                            easing = LinearEasing,
                                        ),
                                ) + expandVertically(expandFrom = Alignment.Bottom),
                            exit = slideOutVertically(animationSpec = tween(durationMillis = 300)),
                        ) {
                            if (isShowBottomMenu) {
                                BottomNavigationBar(
                                    currentTab = (state as? NavigationState.SignedIn)?.currentTab,
                                    onSelect = { channelNavigator.navigate(it) },
                                )
                            }
                        }
                    },
                ) { padding ->
                    state?.let {
                        NavigationHost(
                            state = it,
                            navigationViewModel = navigationViewModel,
                            channelNavigator = channelNavigator,
                            entryProvider = navigationEntryProvider,
                            onExit = ::finish,
                            modifier = Modifier.padding(padding),
                        )
                    }
                }
                if (shouldShowDialog.value) {
                    ExplainWhyWeNeedToUseNotification(shouldShowDialog)
                }
            }
        }
    }

    @Composable
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun CheckNotificationPermission(requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>) {
        SideEffect {
            NotificationManagerCompat.from(this@MainActivity).apply {
                when {
                    !areNotificationsEnabled() -> {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    else -> createNotificationChannel()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val channelId = getString(R.string.notification_channel_id)
        val name = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        // Register the channel with the system
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
        CO2Worker.enqueue(this)
    }

    @Composable
    private fun ExplainWhyWeNeedToUseNotification(shouldShowDialog: MutableState<Boolean>) {
        AlertDialog(
            onDismissRequest = { shouldShowDialog.value = false },
            title = { Text(text = stringResource(id = R.string.enable_notifications)) },
            text = { Text(text = stringResource(id = R.string.explain_why_we_should_enable_notification)) },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.data = Uri.fromParts("package", packageName, null)
                        startActivity(intent)
                        shouldShowDialog.value = false
                    },
                ) {
                    Text(text = stringResource(id = R.string.go_to_settings))
                }
            },
        )
    }
}

@Composable
fun BottomNavigationBar(
    currentTab: TopLevelRoute?,
    onSelect: (TopLevelRoute) -> Unit,
) {
    val shape = remember { RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) }
    NavigationBar(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp)
                .clip(shape = shape),
        containerColor = MaterialTheme.colorScheme.onSurface,
    ) {
        topLevelTabs.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.testTag(item.testTag),
                selected = item.route == currentTab,
                onClick = { onSelect(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = stringResource(item.label),
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.label),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}
