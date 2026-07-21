package com.fielddevice.healthmonitor.data.repository

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import com.fielddevice.healthmonitor.data.model.DeviceInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DeviceInfoRepository {
    suspend fun getDeviceInfo(): DeviceInfo
}

class DeviceInfoRepositoryImpl(
    private val context: Context
) : DeviceInfoRepository {

    override suspend fun getDeviceInfo(): DeviceInfo = withContext(Dispatchers.IO) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo().also { activityManager.getMemoryInfo(it) }

        // The app's private data directory always exists and needs no storage permission,
        // making it a reliable stand-in for "device storage" on modern Android where the
        // legacy external storage root is no longer freely readable.
        val statFs = StatFs(context.filesDir.path)
        val totalStorageBytes = statFs.totalBytes
        val availableStorageBytes = statFs.availableBytes

        val displayMetrics = context.resources.displayMetrics

        DeviceInfo(
            manufacturer = Build.MANUFACTURER,
            model = Build.MODEL,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "unknown",
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            isLowRamDevice = activityManager.isLowRamDevice,
            totalStorageBytes = totalStorageBytes,
            availableStorageBytes = availableStorageBytes,
            screenWidthPx = displayMetrics.widthPixels,
            screenHeightPx = displayMetrics.heightPixels,
            densityDpi = displayMetrics.densityDpi
        )
    }
}
