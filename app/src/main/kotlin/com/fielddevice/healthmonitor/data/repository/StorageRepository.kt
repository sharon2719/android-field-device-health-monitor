package com.fielddevice.healthmonitor.data.repository

import android.content.Context
import android.os.StatFs
import com.fielddevice.healthmonitor.data.model.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.delay

interface StorageRepository {
    suspend fun getStorageInfo(): StorageInfo

    /** Re-reads storage stats every [intervalMillis], so the UI reflects usage as it changes. */
    fun observeStorageInfo(intervalMillis: Long = DEFAULT_POLL_INTERVAL_MS): Flow<StorageInfo>

    companion object {
        const val DEFAULT_POLL_INTERVAL_MS = 5_000L
    }
}

class StorageRepositoryImpl(
    private val context: Context
) : StorageRepository {

    override suspend fun getStorageInfo(): StorageInfo {
        val statFs = StatFs(context.filesDir.path)
        return StorageInfo(
            totalBytes = statFs.totalBytes,
            freeBytes = statFs.availableBytes
        )
    }

    override fun observeStorageInfo(intervalMillis: Long): Flow<StorageInfo> = flow {
        while (true) {
            emit(getStorageInfo())
            delay(intervalMillis)
        }
    }.flowOn(Dispatchers.IO)
}
