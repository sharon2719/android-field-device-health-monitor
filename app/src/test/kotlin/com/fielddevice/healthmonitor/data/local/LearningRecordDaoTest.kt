package com.fielddevice.healthmonitor.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fielddevice.healthmonitor.data.local.dao.LearningRecordDao
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/**
 * Exercises [LearningRecordDao] against a real (in-memory) SQLite database via Room, run
 * through Robolectric so it's a fast JVM test rather than requiring a connected device/emulator.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class LearningRecordDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: LearningRecordDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.learningRecordDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun sampleRecord(
        studentId: String = "student-1",
        status: SyncStatus = SyncStatus.PENDING,
        createdAt: Long = 1_000L
    ) = LearningRecord(
        studentId = studentId,
        activityName = "Fractions Quiz",
        score = 85,
        syncStatus = status,
        createdAt = createdAt
    )

    @Test
    fun `insert then observeAll emits the inserted record`() = runTest {
        dao.insert(sampleRecord())

        val all = dao.observeAll().first()

        assertThat(all).hasSize(1)
        assertThat(all.first().studentId).isEqualTo("student-1")
        assertThat(all.first().syncStatus).isEqualTo(SyncStatus.PENDING)
    }

    @Test
    fun `observeByStatus only returns matching records`() = runTest {
        dao.insert(sampleRecord(studentId = "pending-student", status = SyncStatus.PENDING))
        dao.insert(sampleRecord(studentId = "synced-student", status = SyncStatus.SYNCED))

        val pending = dao.observeByStatus(SyncStatus.PENDING).first()

        assertThat(pending).hasSize(1)
        assertThat(pending.first().studentId).isEqualTo("pending-student")
    }

    @Test
    fun `updateSyncStatus changes only the targeted record`() = runTest {
        val idA = dao.insert(sampleRecord(studentId = "student-a"))
        val idB = dao.insert(sampleRecord(studentId = "student-b"))

        dao.updateSyncStatus(idA, SyncStatus.SYNCED)

        val all = dao.observeAll().first().associateBy { it.studentId }
        assertThat(all.getValue("student-a").syncStatus).isEqualTo(SyncStatus.SYNCED)
        assertThat(all.getValue("student-b").syncStatus).isEqualTo(SyncStatus.PENDING)
        assertThat(idB).isNotEqualTo(idA)
    }

    @Test
    fun `markAllAs moves every pending record to synced`() = runTest {
        dao.insert(sampleRecord(studentId = "student-a"))
        dao.insert(sampleRecord(studentId = "student-b"))

        dao.markAllAs(from = SyncStatus.PENDING, status = SyncStatus.SYNCED)

        val pendingCount = dao.observePendingCount().first()
        assertThat(pendingCount).isEqualTo(0)
    }

    @Test
    fun `count reflects total number of rows`() = runTest {
        assertThat(dao.count()).isEqualTo(0)

        dao.insert(sampleRecord(studentId = "student-a"))
        dao.insert(sampleRecord(studentId = "student-b"))

        assertThat(dao.count()).isEqualTo(2)
    }
}
