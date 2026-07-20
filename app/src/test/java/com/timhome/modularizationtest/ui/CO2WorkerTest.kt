package com.timhome.modularizationtest.ui

import android.Manifest
import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.timhome.core.common.CallStatus
import com.timhome.core.common.Constant.INDICATOR_CO2_ACCEPTABLE_VALUE
import com.timhome.core.common.Constant.INDICATOR_CO2_LOW_DANGER_LEVEL
import com.timhome.core.data.repository.ArduinoRepository
import com.timhome.core.database.AppDatabase
import com.timhome.core.database.dao.CarbonDioxideDao
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.network.api.response.ArduinoResponse
import com.timhome.modularizationtest.R
import io.mockk.clearAllMocks
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val DAY_HOUR = 12
private const val NIGHT_HOUR = 23
private const val NORMAL_CO2 = 1000
private const val FAKE_TEMPERATURE = 21.5
private const val REPEAT_INTERVAL_MINUTES = 15L

/**
 * Fake over the repository boundary: the worker only ever reads one canned result, so a real
 * implementation is cheaper and more honest than restating `coEvery` in every test.
 */
private class FakeArduinoRepository(
    private val result: CallStatus<ArduinoResponse>,
) : ArduinoRepository {
    override suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse> = result

    override fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>> = flowOf(emptyList())
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CO2WorkerTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val dao = mockk<CarbonDioxideDao>(relaxed = true)
    private val appDatabase = mockk<AppDatabase> { every { carbonDioxideDao() } returns dao }

    private val notificationManager
        get() = context.getSystemService(NotificationManager::class.java)

    private val postedNotifications
        get() = shadowOf(notificationManager).allNotifications

    @Before
    fun setUp() {
        CO2Worker.isDangerCO2LevelsInRoom = false
        shadowOf(context).grantPermissions(Manifest.permission.POST_NOTIFICATIONS)
        // notify() is a no-op without a registered channel on O+, so mirror what the app does.
        notificationManager.createNotificationChannel(
            NotificationChannel(
                context.getString(R.string.notification_channel_id),
                context.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH,
            ),
        )
    }

    @After
    fun tearDown() {
        // Companion state is process-global; leaking it would couple these tests together.
        CO2Worker.isDangerCO2LevelsInRoom = false
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun `doWork retries when the repository call fails`() =
        runTest {
            val worker = buildWorker(CallStatus.Error("offline"))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.retry(), result)
            coVerify(exactly = 0) { dao.saveCO2LevelToDB(any()) }
            assertTrue(postedNotifications.isEmpty())
        }

    @Test
    fun `doWork succeeds without saving when co2 is null`() =
        runTest {
            val worker = buildWorker(CallStatus.Success(null))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            coVerify(exactly = 0) { dao.saveCO2LevelToDB(any()) }
            assertTrue(postedNotifications.isEmpty())
        }

    @Test
    fun `doWork saves co2 but suppresses the push during the night window`() =
        runTest {
            val worker = buildWorker(success(INDICATOR_CO2_LOW_DANGER_LEVEL), hour = NIGHT_HOUR)

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            coVerify(exactly = 1) { dao.saveCO2LevelToDB(any()) }
            assertTrue(postedNotifications.isEmpty())
        }

    @Test
    fun `doWork posts the danger push once co2 reaches the danger level`() =
        runTest {
            val worker = buildWorker(success(INDICATOR_CO2_LOW_DANGER_LEVEL))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            assertEquals(
                context.getString(R.string.dangerous_levels_of_co2),
                postedNotifications.single().extras.getString(Notification.EXTRA_TITLE),
            )
            assertTrue(CO2Worker.isDangerCO2LevelsInRoom)
            coVerify(exactly = 1) { dao.saveCO2LevelToDB(any()) }
        }

    @Test
    fun `doWork posts the clean push when co2 drops back below the acceptable value`() =
        runTest {
            CO2Worker.isDangerCO2LevelsInRoom = true
            val worker = buildWorker(success(INDICATOR_CO2_ACCEPTABLE_VALUE - 1))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            assertEquals(
                context.getString(R.string.air_is_clean),
                postedNotifications.single().extras.getString(Notification.EXTRA_TITLE),
            )
            assertFalse(CO2Worker.isDangerCO2LevelsInRoom)
        }

    @Test
    fun `doWork posts nothing when co2 sits in the normal range`() =
        runTest {
            val worker = buildWorker(success(NORMAL_CO2))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            assertTrue(postedNotifications.isEmpty())
            coVerify(exactly = 1) { dao.saveCO2LevelToDB(any()) }
        }

    @Test
    fun `doWork posts nothing when co2 is low but no danger was flagged`() =
        runTest {
            val worker = buildWorker(success(INDICATOR_CO2_ACCEPTABLE_VALUE - 1))

            worker.doWork()

            assertTrue(postedNotifications.isEmpty())
            assertFalse(CO2Worker.isDangerCO2LevelsInRoom)
        }

    @Test
    fun `doWork skips the push when POST_NOTIFICATIONS is denied`() =
        runTest {
            shadowOf(context).denyPermissions(Manifest.permission.POST_NOTIFICATIONS)
            val worker = buildWorker(success(INDICATOR_CO2_LOW_DANGER_LEVEL))

            val result = worker.doWork()

            assertEquals(ListenableWorker.Result.success(), result)
            assertTrue(postedNotifications.isEmpty())
        }

    @Test
    fun `enqueue schedules unique periodic work every 15 minutes requiring network`() {
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setExecutor(SynchronousExecutor()).build(),
        )

        CO2Worker.enqueue(context)

        val workInfo =
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(CO2Worker.WORK_NAME)
                .get()
                .single()
        assertEquals(NetworkType.CONNECTED, workInfo.constraints.requiredNetworkType)
        assertEquals(
            TimeUnit.MINUTES.toMillis(REPEAT_INTERVAL_MINUTES),
            workInfo.periodicityInfo?.repeatIntervalMillis,
        )
    }

    @Test
    fun `isNightPeriod treats 22 00 to 08 00 as night and the rest as day`() {
        val worker = buildWorker(success(NORMAL_CO2))

        listOf(0, 7, 22, 23).forEach { hour ->
            worker.clock = clockAtHour(hour)
            assertTrue(worker.isNightPeriod(), "hour $hour should be night")
        }
        listOf(8, 12, 21).forEach { hour ->
            worker.clock = clockAtHour(hour)
            assertFalse(worker.isNightPeriod(), "hour $hour should be day")
        }
    }

    private fun success(co2: Int) =
        CallStatus.Success(ArduinoResponse(temperature = FAKE_TEMPERATURE, co2 = co2))

    private fun clockAtHour(hour: Int): Clock =
        Clock.fixed(
            LocalDateTime.of(2026, 7, 20, hour, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )

    private fun buildWorker(
        result: CallStatus<ArduinoResponse>,
        hour: Int = DAY_HOUR,
    ): CO2Worker =
        TestListenableWorkerBuilder<CO2Worker>(context).build().apply {
            arduinoRepository = FakeArduinoRepository(result)
            database = appDatabase
            clock = clockAtHour(hour)
        }
}
