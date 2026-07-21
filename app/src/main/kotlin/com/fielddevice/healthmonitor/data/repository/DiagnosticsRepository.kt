package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.model.DiagnosticCheckId
import com.fielddevice.healthmonitor.data.model.DiagnosticCheckResult
import com.fielddevice.healthmonitor.data.model.DiagnosticsReport
import com.fielddevice.healthmonitor.data.model.StorageHealthStatus
import kotlinx.coroutines.flow.first

/**
 * Runs the "Run Device Check" test suite: five independent, read-only checks that together
 * decide whether a tablet is fit to hand over for deployment. Each check is isolated in its
 * own try/catch so one failing subsystem (e.g. a battery read glitch) can't crash the whole
 * report — it just fails that one check, which is exactly the signal a field technician needs.
 */
interface DiagnosticsRepository {
    suspend fun runDiagnostics(): DiagnosticsReport
}

class DiagnosticsRepositoryImpl(
    private val deviceInfoRepository: DeviceInfoRepository,
    private val batteryRepository: BatteryRepository,
    private val storageRepository: StorageRepository,
    private val networkRepository: NetworkRepository,
    private val learningRecordRepository: LearningRecordRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : DiagnosticsRepository {

    override suspend fun runDiagnostics(): DiagnosticsReport {
        val checks = listOf(
            checkBattery(),
            checkStorage(),
            checkNetwork(),
            checkDeviceInfo(),
            checkDatabase()
        )
        return DiagnosticsReport(checks = checks, generatedAtMillis = clock())
    }

    private suspend fun checkBattery(): DiagnosticCheckResult = runCatching {
        val battery = batteryRepository.observeBatteryInfo().first()
        val passed = battery.percentage in 0..100
        DiagnosticCheckResult(
            id = DiagnosticCheckId.BATTERY_STATUS,
            passed = passed,
            detail = if (passed) "${battery.percentage}% · ${battery.chargingState.name.lowercase()}"
            else "Battery level could not be read"
        )
    }.getOrElse { error ->
        DiagnosticCheckResult(DiagnosticCheckId.BATTERY_STATUS, passed = false, detail = "Unavailable: ${error.message}")
    }

    private suspend fun checkStorage(): DiagnosticCheckResult = runCatching {
        val storage = storageRepository.getStorageInfo()
        val passed = storage.healthStatus != StorageHealthStatus.CRITICAL
        DiagnosticCheckResult(
            id = DiagnosticCheckId.STORAGE_AVAILABILITY,
            passed = passed,
            detail = "${storage.usagePercent}% used · ${storage.healthStatus.name.lowercase()}"
        )
    }.getOrElse { error ->
        DiagnosticCheckResult(DiagnosticCheckId.STORAGE_AVAILABILITY, passed = false, detail = "Unavailable: ${error.message}")
    }

    private suspend fun checkNetwork(): DiagnosticCheckResult = runCatching {
        // An offline device is a normal, supported state for this app — the check verifies
        // connectivity status is *readable*, not that the device is online.
        val network = networkRepository.observeNetworkInfo().first()
        val detail = if (network.isConnected) "Online · ${network.connectionType.name.lowercase()}" else "Offline mode"
        DiagnosticCheckResult(DiagnosticCheckId.NETWORK_CONNECTIVITY, passed = true, detail = detail)
    }.getOrElse { error ->
        DiagnosticCheckResult(DiagnosticCheckId.NETWORK_CONNECTIVITY, passed = false, detail = "Unavailable: ${error.message}")
    }

    private suspend fun checkDeviceInfo(): DiagnosticCheckResult = runCatching {
        val device = deviceInfoRepository.getDeviceInfo()
        DiagnosticCheckResult(
            id = DiagnosticCheckId.DEVICE_INFO_AVAILABLE,
            passed = true,
            detail = "${device.manufacturer} ${device.model} · API ${device.sdkInt}"
        )
    }.getOrElse { error ->
        DiagnosticCheckResult(DiagnosticCheckId.DEVICE_INFO_AVAILABLE, passed = false, detail = "Unavailable: ${error.message}")
    }

    private suspend fun checkDatabase(): DiagnosticCheckResult = runCatching {
        val count = learningRecordRepository.recordCount()
        DiagnosticCheckResult(
            id = DiagnosticCheckId.DATABASE_AVAILABLE,
            passed = true,
            detail = "$count record(s) stored locally"
        )
    }.getOrElse { error ->
        DiagnosticCheckResult(DiagnosticCheckId.DATABASE_AVAILABLE, passed = false, detail = "Unavailable: ${error.message}")
    }
}
