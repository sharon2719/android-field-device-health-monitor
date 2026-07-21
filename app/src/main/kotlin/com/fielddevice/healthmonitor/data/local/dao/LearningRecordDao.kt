package com.fielddevice.healthmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * Data access object for [LearningRecord]. All reads are exposed as [Flow]s so the UI
 * layer automatically recomposes whenever a record is added or its sync status changes,
 * without any manual cache invalidation.
 */
@Dao
interface LearningRecordDao {

    @Insert
    suspend fun insert(record: LearningRecord): Long

    @Update
    suspend fun update(record: LearningRecord)

    @Query("SELECT * FROM learning_records ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<LearningRecord>>

    @Query("SELECT * FROM learning_records WHERE syncStatus = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: SyncStatus): Flow<List<LearningRecord>>

    @Query("SELECT COUNT(*) FROM learning_records WHERE syncStatus = :status")
    fun observePendingCount(status: SyncStatus = SyncStatus.PENDING): Flow<Int>

    @Query("UPDATE learning_records SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus)

    @Query("UPDATE learning_records SET syncStatus = :status WHERE syncStatus = :from")
    suspend fun markAllAs(from: SyncStatus, status: SyncStatus)

    @Query("SELECT COUNT(*) FROM learning_records")
    suspend fun count(): Int
}
