package com.example.modularizationtest.ui

import android.Manifest
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.base.DaggerBaseComponent
import com.example.core.coreComponent
import com.example.core.data.AppDatabase
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.data.repository.ArduinoRepository
import com.example.core.utils.CallStatus
import com.example.core.utils.Constant.INDICATOR_CO2_ACCEPTABLE_VALUE
import com.example.core.utils.Constant.INDICATOR_CO2_LOW_DANGER_LEVEL
import com.example.modularizationtest.R
import com.example.modularizationtest.di.DaggerAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

private const val START_NIGHT = 22
private const val END_NIGHT = 8

class CO2JobService : JobService() {
    private var job: Job? = null

    @Inject
    lateinit var arduinoRepository: ArduinoRepository

    @Inject
    lateinit var database: AppDatabase

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        DaggerAppComponent
            .factory()
            .create(baseComponent = DaggerBaseComponent.factory().create(coreComponent()))
            .inject(this)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        job =
            CoroutineScope(Dispatchers.Default).launch {
                jobFinished(params, true)
                if (coroutineContext.isActive) getCO2()
            }
        return true // Indicates that the job is still running
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        job?.cancel()
        return true // Indicates that the job should be rescheduled
    }

    @Suppress("MagicNumber")
    private suspend fun getCO2() {
        withContext(Dispatchers.IO) {
            // TODO check if user set baseurl before call
            when (val result = arduinoRepository.getCo2AndTemperature()) {
                is CallStatus.Success -> {
                    val co2 = result.data?.co2 ?: return@withContext
                    saveToDataBase(co2)
                    when {
                        isNightPeriod() -> return@withContext
                        co2 >= INDICATOR_CO2_LOW_DANGER_LEVEL -> {
                            isDangerCO2LevelsInRoom = true
                            sendNotification(
                                title = getString(R.string.dangerous_levels_of_co2),
                                text = getString(R.string.value_levels_of_co2, co2),
                                bigText =
                                    getString(
                                        R.string.notification_message_levels_of_co2,
                                        co2,
                                    ),
                            )
                        }
                        isDangerCO2LevelsInRoom && co2 < INDICATOR_CO2_ACCEPTABLE_VALUE -> {
                            isDangerCO2LevelsInRoom = false
                            sendNotification(
                                title = getString(R.string.air_is_clean),
                                text = getString(R.string.co2_level_returned_to_normal),
                                bigText =
                                    getString(
                                        R.string.co2_level_returned_to_normal_long_text,
                                        co2,
                                    ),
                            )
                        }
                    }
                }
                else -> {
                    // TODO send notification that service is not response
                    return@withContext
                }
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
        val channelId = getString(R.string.notification_channel_id)
        // Create the PendingIntent
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        // Create the notification builder
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(com.example.core.R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))

        // Display the notification
        NotificationManagerCompat.from(this).apply {
            if (ActivityCompat.checkSelfPermission(
                    this@CO2JobService,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e("0707", "=================sendNotification======================")
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }

    private fun isNightPeriod(): Boolean {
        val currentDateTime = LocalDateTime.now()
        val currentHour = currentDateTime.hour
        val currentMinute = currentDateTime.minute
        return currentHour < END_NIGHT || currentHour > START_NIGHT && currentMinute > 0
    }

    companion object {
        private const val NOTIFICATION_ID = 1025
        private var isDangerCO2LevelsInRoom = false
    }
}
