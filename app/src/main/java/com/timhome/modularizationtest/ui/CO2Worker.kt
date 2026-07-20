package com.timhome.modularizationtest.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.timhome.base.DaggerBaseComponent
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.CallStatus
import com.timhome.core.common.Constant.INDICATOR_CO2_ACCEPTABLE_VALUE
import com.timhome.core.common.Constant.INDICATOR_CO2_LOW_DANGER_LEVEL
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.database.AppDatabase
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.modularizationtest.R
import com.timhome.modularizationtest.di.DaggerAppComponent
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val START_NIGHT = 22
private const val END_NIGHT = 8
private const val TAG = "CO2Worker"

class CO2Worker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    @Inject
    lateinit var arduinoRepository: ArduinoRepository

    @Inject
    lateinit var database: AppDatabase

    init {
        DaggerAppComponent
            .factory()
            .create(
                baseComponent =
                    DaggerBaseComponent.factory()
                        .create(ModularizationApplication.coreComponent(appContext)),
            )
            .inject(this)
    }

    @AddTrace(name = "co2Worker", enabled = true)
    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork() started (runAttempt=$runAttemptCount)")
        val result = getCO2()
        Log.d(TAG, "doWork() finished with result=$result")
        return result
    }

    @Suppress("MagicNumber")
    private suspend fun getCO2(): Result =
        withContext(Dispatchers.IO) {
            // TODO check if user set baseurl before call
            when (val result = arduinoRepository.getCo2AndTemperature()) {
                is CallStatus.Success -> {
                    val co2 = result.data?.co2
                    Log.d(TAG, "getCO2() repository Success, co2=$co2")
                    if (co2 == null) {
                        Log.w(TAG, "getCO2() co2 is null, nothing to do")
                        return@withContext Result.success()
                    }
                    saveToDataBase(co2)
                    when {
                        isNightPeriod() -> {
                            Log.d(TAG, "getCO2() night period, notification suppressed")
                            return@withContext Result.success()
                        }
                        co2 >= INDICATOR_CO2_LOW_DANGER_LEVEL -> {
                            Log.d(TAG, "getCO2() co2=$co2 >= danger($INDICATOR_CO2_LOW_DANGER_LEVEL), sending danger push")
                            isDangerCO2LevelsInRoom = true
                            sendNotification(
                                title = applicationContext.getString(R.string.dangerous_levels_of_co2),
                                text = applicationContext.getString(R.string.value_levels_of_co2, co2),
                                bigText =
                                    applicationContext.getString(
                                        R.string.notification_message_levels_of_co2,
                                        co2,
                                    ),
                            )
                        }
                        isDangerCO2LevelsInRoom && co2 < INDICATOR_CO2_ACCEPTABLE_VALUE -> {
                            Log.d(TAG, "getCO2() co2=$co2 back to normal, sending clean push")
                            isDangerCO2LevelsInRoom = false
                            sendNotification(
                                title = applicationContext.getString(R.string.air_is_clean),
                                text = applicationContext.getString(R.string.co2_level_returned_to_normal),
                                bigText =
                                    applicationContext.getString(
                                        R.string.co2_level_returned_to_normal_long_text,
                                        co2,
                                    ),
                            )
                        }
                        else -> Log.d(TAG, "getCO2() co2=$co2 within normal range, no push")
                    }
                    Result.success()
                }
                else -> {
                    // TODO send notification that service is not response
                    Log.w(TAG, "getCO2() repository returned error/$result, will retry")
                    Result.retry()
                }
            }
        }

    private suspend fun saveToDataBase(co2: Int) {
        database.carbonDioxideDao().saveCO2LevelToDB(
            CarbonDioxideEntity(co2Level = co2, date = LocalDateTime.now().toString()),
        )
    }

    private fun sendNotification(
        title: String,
        text: String,
        bigText: String,
    ) {
        val channelId = applicationContext.getString(R.string.notification_channel_id)
        // Create the PendingIntent
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        // Create the notification builder
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))

        // Display the notification
        NotificationManagerCompat.from(applicationContext).apply {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "sendNotification() POST_NOTIFICATIONS not granted, cannot post '$title'")
                return
            }
            Log.d(TAG, "sendNotification() posting id=$NOTIFICATION_ID title='$title'")
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun isNightPeriod(): Boolean {
        val currentHour = LocalDateTime.now().hour
        // Night = 22:00–08:00; notifications are suppressed during this window.
        return currentHour < END_NIGHT || currentHour >= START_NIGHT
    }

    companion object {
        private const val NOTIFICATION_ID = 1025
        private const val WORK_NAME = "co2_periodic_work"
        private const val REPEAT_INTERVAL_MINUTES = 15L
        private var isDangerCO2LevelsInRoom = false

        /**
         * Enqueues the periodic CO2 check. WorkManager persists this across reboots, so no
         * boot receiver is needed. [ExistingPeriodicWorkPolicy.KEEP] preserves an already
         * scheduled request instead of resetting its interval on every call.
         */
        fun enqueue(context: Context) {
            val constraints =
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            val request =
                PeriodicWorkRequestBuilder<CO2Worker>(REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
            Log.d(TAG, "enqueue() periodic CO2 work scheduled (every $REPEAT_INTERVAL_MINUTES min)")
        }
    }
}
