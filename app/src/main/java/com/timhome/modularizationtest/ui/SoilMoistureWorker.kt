package com.timhome.modularizationtest.ui

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.timhome.base.DaggerBaseComponent
import com.timhome.core.ModularizationApplication
import com.timhome.core.data.repository.SoilMoistureRepository
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.modularizationtest.R
import com.timhome.modularizationtest.di.DaggerAppComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SoilMoistureWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    @Inject
    lateinit var soilMoistureRepository: SoilMoistureRepository

    init {
        DaggerAppComponent
            .factory()
            .create(
                baseComponent =
                    DaggerBaseComponent.factory().create(ModularizationApplication.coreComponent(context)),
            )
            .inject(this)
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val ineffective = soilMoistureRepository.pollAllRooms()
                ineffective.forEach { (pot, room) -> sendIneffectiveWateringNotification(pot, room) }
                Result.success()
            } catch (exception: Exception) {
                Result.retry()
            }
        }
    }

    private fun sendIneffectiveWateringNotification(
        pot: PotEntity,
        room: RoomEntity,
    ) {
        val channelId = applicationContext.getString(R.string.notification_channel_id)
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(
                applicationContext,
                pot.id,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val text = applicationContext.getString(R.string.watering_ineffective_text, pot.name, room.name)
        val builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(applicationContext.getString(R.string.watering_ineffective_title))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))

        NotificationManagerCompat.from(applicationContext).apply {
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(NOTIFICATION_ID_BASE + pot.id, builder.build())
        }
    }

    companion object {
        private const val NOTIFICATION_ID_BASE = 2000
    }
}
