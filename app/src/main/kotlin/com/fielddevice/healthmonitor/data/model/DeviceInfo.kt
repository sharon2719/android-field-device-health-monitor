package com.fielddevice.healthmonitor.data.model

/**
 * Static-ish hardware/software identity of the device. Everything here comes from
 * platform APIs that require no runtime permission: [android.os.Build], [android.os.Build.VERSION],
 * [android.app.ActivityManager.getMemoryInfo], [android.util.DisplayMetrics] and
 * [android.os.StatFs].
 */
data class DeviceInfo(
    val manufacturer: String,
    val model: String,
    val androidVersion: String,
    val sdkInt: Int,
    val cpuAbi: String,
    val totalRamBytes: Long,
    val availableRamBytes: Long,
    val isLowRamDevice: Boolean,
    val totalStorageBytes: Long,
    val availableStorageBytes: Long,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val densityDpi: Int
) {
    val screenResolution: String get() = "$screenWidthPx x $screenHeightPx"
}
