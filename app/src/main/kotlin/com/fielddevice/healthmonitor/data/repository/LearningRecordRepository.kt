package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.local.dao.LearningRecordDao
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first store for tablet assessment activity. Every write lands in Room
 * immediately and is readable by the UI with no network round trip; [SyncStatus] tracks
 * what still needs to reach a backend whenever connectivity is next available.
 */
interface LearningRecordRepository {
    fun observeAllRecords(): Flow<List<LearningRecord>>
    fun observePendingRecords(): Flow<List<LearningRecord>>
    fun observePendingCount(): Flow<Int>

    suspend fun addRecord(studentId: String, activityName: String, score: Int): Long
    suspend fun markSynced(id: Long)
    suspend fun markAllPendingSynced()
    suspend fun recordCount(): Int
}

class LearningRecordRepositoryImpl(
    private val dao: LearningRecordDao,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : LearningRecordRepository {

    override fun observeAllRecords(): Flow<List<LearningRecord>> = dao.observeAll()

    override fun observePendingRecords(): Flow<List<LearningRecord>> =
        dao.observeByStatus(SyncStatus.PENDING)

    override fun observePendingCount(): Flow<Int> = dao.observePendingCount()

    override suspend fun addRecord(studentId: String, activityName: String, score: Int): Long {
        require(studentId.isNotBlank()) { "studentId must not be blank" }
        require(activityName.isNotBlank()) { "activityName must not be blank" }
        require(score in 0..100) { "score must be between 0 and 100" }

        val record = LearningRecord(
            studentId = studentId.trim(),
            activityName = activityName.trim(),
            score = score,
            syncStatus = SyncStatus.PENDING,
            createdAt = clock()
        )
        return dao.insert(record)
    }

    override suspend fun markSynced(id: Long) {
        dao.updateSyncStatus(id, SyncStatus.SYNCED)
    }

    override suspend fun markAllPendingSynced() {
        dao.markAllAs(from = SyncStatus.PENDING, status = SyncStatus.SYNCED)
    }

    override suspend fun recordCount(): Int = dao.count()
}
