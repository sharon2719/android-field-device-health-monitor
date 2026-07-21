package com.fielddevice.healthmonitor.data.model

/**
 * Storage health snapshot derived from [android.os.StatFs] on the app's data directory,
 * which (unlike the shared external volume) requires no storage permission to query.
 */
data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long
) {
    val usedBytes: Long get() = totalBytes - freeBytes

    val usagePercent: Int
        get() = if (totalBytes <= 0L) 0 else ((usedBytes * 100) / totalBytes).toInt()

    val healthStatus: StorageHealthStatus
        get() = when {
            usagePercent >= CRITICAL_THRESHOLD_PERCENT -> StorageHealthStatus.CRITICAL
            usagePercent >= WARNING_THRESHOLD_PERCENT -> StorageHealthStatus.WARNING
            else -> StorageHealthStatus.HEALTHY
        }

    companion object {
        const val WARNING_THRESHOLD_PERCENT = 80
        const val CRITICAL_THRESHOLD_PERCENT = 90
    }
}

enum class StorageHealthStatus {
    HEALTHY,
    WARNING,
    CRITICAL
}
