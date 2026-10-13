package com.timhome.device.data.repository

import androidx.room.Room
import com.timhome.core.database.AppDatabase
import com.timhome.device.data.mappers.DeviceMapper
import com.timhome.device.data.mock.GenerateDate
import com.timhome.device.data.model.Device
import com.timhome.device.data.model.ModuleModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

/** Runs against a real in-memory database, with the real mapper and the real mock data. */
@RunWith(RobolectricTestRunner::class)
class DeviceRepositoryTest {
    private val database =
        Room.inMemoryDatabaseBuilder(RuntimeEnvironment.getApplication(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    private val mapper = DeviceMapper()
    private val mock = GenerateDate()
    private val repository = DeviceRepository(database, mapper, mock)

    @After
    fun tearDown() {
        database.close()
    }

    private suspend fun mockDevices(): List<Device> = mock.generateItems().first()

    /** The device in the mock data that has modules, and its modules as they are read back. */
    private suspend fun deviceWithModules(): Pair<Device, List<ModuleModel>> {
        val device = mockDevices().first { it.modules.isNotEmpty() }
        return device to device.modules
    }

    @Test
    fun `getDeviceById returns null for an unknown id`() =
        runTest {
            assertNull(repository.getDeviceById(1))

            repository.initAllDevices()

            assertNull(repository.getDeviceById(UNKNOWN_ID))
        }

    @Test
    fun `getDeviceById returns the mapped model for a known id`() =
        runTest {
            repository.initAllDevices()
            val expected = mockDevices().first()

            val device = repository.getDeviceById(expected.id)

            assertEquals(mapper.convertEntityToModel(mapper.convertToEntityModel(expected)), device)
            assertEquals(expected.title, device?.title)
        }

    @Test
    fun `getAllDevices is empty before initAllDevices`() =
        runTest {
            assertTrue(repository.getAllDevices().first().isEmpty())
        }

    @Test
    fun `initAllDevices stores every mock device`() =
        runTest {
            repository.initAllDevices()

            val devices = repository.getAllDevices().first()

            assertEquals(mockDevices().map { it.id }, devices.map { it.id })
            assertEquals(mockDevices().map { it.title }, devices.map { it.title })
        }

    @Test
    fun `initAllDevices twice does not duplicate devices`() =
        runTest {
            repository.initAllDevices()
            repository.initAllDevices()

            assertEquals(mockDevices().size, repository.getAllDevices().first().size)
        }

    @Test
    fun `getSelectedDeviceById returns the mapped model`() =
        runTest {
            repository.initAllDevices()
            val expected = mockDevices().last()

            val device = repository.getSelectedDeviceById(expected.id)

            assertEquals(mapper.convertEntityToModel(mapper.convertToEntityModel(expected)), device)
        }

    @Test
    fun `getModuleToBuyByDeviceId returns only that device's modules`() =
        runTest {
            repository.initAllDevices()
            val (device, modules) = deviceWithModules()
            val deviceWithoutModules = mockDevices().first { it.modules.isEmpty() }

            assertEquals(modules, repository.getModuleToBuyByDeviceId(device.id).first())
            assertTrue(repository.getModuleToBuyByDeviceId(deviceWithoutModules.id).first().isEmpty())
        }

    @Test
    fun `no module is selected to buy at first`() =
        runTest {
            repository.initAllDevices()
            val (device, _) = deviceWithModules()

            assertTrue(repository.getSelectedModuleToBuy().first().isEmpty())
            assertTrue(repository.getSelectedModuleToBuyByDeviceId(device.id).first().isEmpty())
        }

    @Test
    fun `selectModuleToBuy selects a module that is not selected`() =
        runTest {
            repository.initAllDevices()
            val (device, modules) = deviceWithModules()
            val otherDevice = mockDevices().first { it.id != device.id }
            val module = modules.first()

            repository.selectModuleToBuy(module)

            val selected = listOf(module.copy(isSelectToBuy = true))
            assertEquals(selected, repository.getSelectedModuleToBuy().first())
            assertEquals(selected, repository.getSelectedModuleToBuyByDeviceId(device.id).first())
            assertTrue(repository.getSelectedModuleToBuyByDeviceId(otherDevice.id).first().isEmpty())
        }

    @Test
    fun `selectModuleToBuy clears a module that is selected`() =
        runTest {
            repository.initAllDevices()
            val (device, modules) = deviceWithModules()
            repository.selectModuleToBuy(modules.first())
            val selected = repository.getSelectedModuleToBuyByDeviceId(device.id).first().single()

            repository.selectModuleToBuy(selected)

            assertTrue(repository.getSelectedModuleToBuy().first().isEmpty())
            assertTrue(repository.getSelectedModuleToBuyByDeviceId(device.id).first().isEmpty())
        }

    private companion object {
        const val UNKNOWN_ID = 999
    }
}
