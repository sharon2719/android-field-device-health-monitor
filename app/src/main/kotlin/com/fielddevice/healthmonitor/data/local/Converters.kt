package com.fielddevice.healthmonitor.data.local

import androidx.room.TypeConverter
import com.fielddevice.healthmonitor.data.local.entity.SyncStatus

/**
 * Room persists SQLite primitives only, so enum columns need an explicit mapping to and
 * from [String]. Storing the enum name (rather than its ordinal) keeps the database
 * self-describing and safe to reorder in code without corrupting existing rows.
 */
class Converters {

    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}
