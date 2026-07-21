package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.local.dao.LearningRecordDao
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LearningRecordRepositoryTest {

    private val dao: LearningRecordDao = mockk(relaxed = true)
    private lateinit var repository: LearningRecordRepositoryImpl

    @Before
    fun setUp() {
        repository = LearningRecordRepositoryImpl(dao, clock = { FIXED_TIME })
    }

    @Test
    fun `addRecord persists a pending record with trimmed fields`() = runTest {
        val recordSlot = slot<LearningRecord>()
        coEvery { dao.insert(capture(recordSlot)) } returns 42L

        val id = repository.addRecord(studentId = "  s-1  ", activityName = "  Reading  ", score = 90)

        assertThat(id).isEqualTo(42L)
        assertThat(recordSlot.captured.studentId).isEqualTo("s-1")
        assertThat(recordSlot.captured.activityName).isEqualTo("Reading")
        assertThat(recordSlot.captured.score).isEqualTo(90)
        assertThat(recordSlot.captured.syncStatus).isEqualTo(SyncStatus.PENDING)
        assertThat(recordSlot.captured.createdAt).isEqualTo(FIXED_TIME)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `addRecord rejects a blank studentId`() = runTest {
        repository.addRecord(studentId = "   ", activityName = "Reading", score = 50)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `addRecord rejects an out-of-range score`() = runTest {
        repository.addRecord(studentId = "s-1", activityName = "Reading", score = 150)
    }

    @Test
    fun `markSynced delegates to dao with SYNCED status`() = runTest {
        repository.markSynced(7L)

        coVerify { dao.updateSyncStatus(7L, SyncStatus.SYNCED) }
    }

    @Test
    fun `markAllPendingSynced moves every pending record to synced`() = runTest {
        repository.markAllPendingSynced()

        coVerify { dao.markAllAs(from = SyncStatus.PENDING, status = SyncStatus.SYNCED) }
    }

    companion object {
        private const val FIXED_TIME = 1_700_000_000_000L
    }
}
