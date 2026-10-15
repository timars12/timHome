package com.timhome.core.data.repository

import android.app.Application
import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.datastore.getDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Runs against the real Preferences DataStore, in Robolectric's temporary files directory. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DataStoreSessionRepositoryTest {
    private val context: Application = RuntimeEnvironment.getApplication()
    private val dataStoreManager = DataStoreManager(context)
    private val repository = DataStoreSessionRepository(dataStoreManager)

    @After
    fun tearDown() {
        // The DataStore is one instance per process, so a stored value would leak into the next test.
        runBlocking { dataStoreManager.clearData() }
    }

    @Test
    fun `S1 the status is false when nothing is stored`() =
        runTest {
            assertFalse(repository.isSignedIn.first())
        }

    @Test
    fun `S2 the status is true after markSignedIn, also in a new instance`() =
        runTest {
            repository.markSignedIn()

            assertTrue(repository.isSignedIn.first())
            assertTrue(DataStoreSessionRepository(DataStoreManager(context)).isSignedIn.first())
        }

    @Test
    fun `S2 markSignedIn is idempotent`() =
        runTest {
            repository.markSignedIn()
            repository.markSignedIn()

            assertTrue(repository.isSignedIn.first())
        }

    @Test
    fun `S3 clearData resets the status to false`() =
        runTest {
            repository.markSignedIn()

            dataStoreManager.clearData()

            assertFalse(repository.isSignedIn.first())
        }

    @Test
    fun `S4 only the isSignedIn key is written, as a boolean`() =
        runTest {
            repository.markSignedIn()

            val stored = context.getDataStore.data.first().asMap()

            assertEquals(listOf("isSignedIn"), stored.keys.map { it.name })
            assertEquals(listOf<Any>(true), stored.values.toList())
        }
}
