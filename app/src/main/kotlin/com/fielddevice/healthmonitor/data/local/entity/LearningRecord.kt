package com.fielddevice.healthmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single offline learning/assessment record captured on a field tablet.
 *
 * Field tablets deployed to schools or clinics with intermittent connectivity cannot
 * assume a live connection to a backend. Every assessment a student completes is written
 * to the local Room database first ([syncStatus] = [SyncStatus.PENDING]) and only marked
 * [SyncStatus.SYNCED] once a background sync job (out of scope for this simulation)
 * successfully uploads it. This is the core "offline-first" pattern this app demonstrates.
 */
@Entity(tableName = "learning_records")
data class LearningRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val studentId: String,
    val activityName: String,
    val score: Int,
    val syncStatus: SyncStatus,
    val createdAt: Long
)

/**
 * Sync lifecycle of a [LearningRecord] against a (simulated) remote backend.
 */
enum class SyncStatus {
    PENDING,
    SYNCED
}
