package com.example.modularizationtest.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.authdynamic.ui.signin.navigation.signInRoute
import com.example.base.DaggerBaseComponent
import com.example.core.coreComponent
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.NavigationDispatcher
import com.example.device.ui.navigation.deviceRoute
import com.example.home.ui.navigation.homeRoute
import com.example.settings.ui.navigation.settingRoute
import com.example.modularizationtest.R
import com.example.modularizationtest.data.BottomNavigationMenuItem
import com.example.modularizationtest.di.DaggerAppComponent
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import okhttp3.internal.immutableListOf
import javax.inject.Inject

class MainActivity : ComponentActivity() {
    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

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
            val navController: NavHostController = rememberNavController()
            val isShowBottomMenu = remember { mutableStateOf(false) }
            val shouldShowDialog = remember { mutableStateOf(false) }

            val requestPermissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    when {
                        isGranted -> createNotificationChannel()
                        else -> shouldShowDialog.value = true
                    }
                }

            CheckNotificationPermission(requestPermissionLauncher)
            LaunchedEffect(Unit) {
                observeNavigationCommands(navController)
            }
            DisposableEffect(Unit) {
                val destinationChangedListener =
                    NavController.OnDestinationChangedListener { _, destination, _ ->
                        isShowBottomMenu.value =
                            !destination.route.isNullOrEmpty() && destination.route != "signInScreen"
                    }
                navController.addOnDestinationChangedListener(destinationChangedListener)
                onDispose {
                    navController.removeOnDestinationChangedListener(destinationChangedListener)
                }
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
                            visible = isShowBottomMenu.value,
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
                            if (isShowBottomMenu.value) BottomNavigationBar(navController)
                        }
                    },
                ) { padding ->
                    NavHost(
                        modifier = Modifier.padding(padding),
                        navController = navController,
                        startDestination = "signInScreen",
                    ) {
                        signInRoute()
                        homeRoute()
                        deviceRoute()
                        settingRoute()
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

    @AddTrace(name = "startCo2JobService", enabled = true)
    private fun startCo2JobService() {
        val jobId = 1 // Unique job ID
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobs = jobScheduler.allPendingJobs.singleOrNull { jobInfo -> jobInfo.id == jobId }

        if (jobs == null) {
            val jobInfo =
                JobInfo.Builder(jobId, ComponentName(this, CO2JobService::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Requires a network connection
                    .setPeriodic(AlarmManager.INTERVAL_FIFTEEN_MINUTES) // Sets the job to repeat every 5 minutes
                    .build()

            jobScheduler.schedule(jobInfo)
        }
    }

    private fun createNotificationChannel() {
        val channelId = getString(R.string.notification_channel_id)
        val name = getString(R.string.app_name)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelId, name, importance)
        // Register the channel with the system
        NotificationManagerCompat.from(this).createNotificationChannel(channel)
        startCo2JobService()
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

    private suspend fun observeNavigationCommands(navController: NavHostController) {
        navigationDispatcher.navigationEmitter.receiveAsFlow()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect { command ->
                command.invoke(navController)
            }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val shape = remember { RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) }
    val menuItems =
        remember {
            immutableListOf(
                BottomNavigationMenuItem(
                    destinationName = "homeScreen",
                    label = R.string.home,
                    icon = R.drawable.ic_home_bottom_menu,
                    testTag = "bnv_home",
                ),
                BottomNavigationMenuItem(
                    destinationName = "devicesScreen",
                    label = R.string.device,
                    icon = R.drawable.ic_device_bottom_menu,
                    testTag = "bnv_device",
                ),
                BottomNavigationMenuItem(
                    destinationName = "settingScreen",
                    label = R.string.setting,
                    icon = R.drawable.ic_setting_bottom_menu,
                    testTag = "bnv_setting",
                ),
            )
        }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 8.dp)
                .clip(shape = shape),
        containerColor = MaterialTheme.colorScheme.onSurface,
    ) {
        menuItems.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.testTag(item.testTag),
                selected = currentDestination?.hierarchy?.any { it.route == item.destinationName } == true,
                onClick = {
                    if (currentDestination?.route == item.destinationName) return@NavigationBarItem

                    navController.navigate(item.destinationName) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                },
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
