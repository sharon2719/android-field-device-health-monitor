package com.fielddevice.healthmonitor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fielddevice.healthmonitor.data.local.dao.LearningRecordDao
import com.fielddevice.healthmonitor.data.local.entity.LearningRecord

/**
 * Single Room database for the app. A field diagnostics tool has no business depending on
 * network availability to persist its own data, so every entity here is designed to be
 * fully usable offline; [LearningRecord.syncStatus] tracks what still needs to reach a
 * backend once connectivity returns.
 */
@Database(
    entities = [LearningRecord::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun learningRecordDao(): LearningRecordDao

    companion object {
        const val DATABASE_NAME = "field_device_health_monitor.db"
    }
}
