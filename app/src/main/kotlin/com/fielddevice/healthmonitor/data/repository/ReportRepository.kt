package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.model.DeviceReport
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Builds the shareable plain-text "ANDROID DEVICE REPORT" a field technician can paste into
 * a ticket, email, or paper deployment checklist as evidence a tablet was verified.
 */
interface ReportRepository {
    suspend fun generateReport(): DeviceReport
}

class ReportRepositoryImpl(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val batteryRepository: BatteryRepository,
    private val storageRepository: StorageRepository,
    private val networkRepository: NetworkRepository,
    private val learningRecordRepository: LearningRecordRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ReportRepository {

    override suspend fun generateReport(): DeviceReport {
        val device = deviceInfoRepository.getDeviceInfo()
        val battery = batteryRepository.observeBatteryInfo().first()
        val storage = storageRepository.getStorageInfo()
        val network = networkRepository.observeNetworkInfo().first()
        val pendingSyncCount = learningRecordRepository.observePendingCount().first()
        val totalRecords = learningRecordRepository.recordCount()

        val generatedAtMillis = clock()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        val generatedAt = dateFormat.format(Date(generatedAtMillis))

        val reportText = buildString {
            appendLine("ANDROID DEVICE REPORT")
            appendLine()
            appendLine("Model:")
            appendLine("${device.manufacturer} ${device.model}")
            appendLine()
            appendLine("Android:")
            appendLine("${device.androidVersion} (API ${device.sdkInt})")
            appendLine()
            appendLine("Battery:")
            appendLine("${battery.percentage}% · ${battery.chargingState.name.lowercase().replaceFirstChar(Char::uppercase)}")
            appendLine()
            appendLine("Storage:")
            appendLine("${storage.healthStatus.name.lowercase().replaceFirstChar(Char::uppercase)} (${storage.usagePercent}% used)")
            appendLine()
            appendLine("Network:")
            appendLine(if (network.isConnected) network.connectionType.name.lowercase().replaceFirstChar(Char::uppercase) else "Offline")
            appendLine()
            appendLine("Database:")
            appendLine("$totalRecords record(s), $pendingSyncCount pending sync")
            appendLine()
            appendLine("Generated:")
            append(generatedAt)
        }

        return DeviceReport(reportText = reportText, generatedAtMillis = generatedAtMillis)
    }
}
