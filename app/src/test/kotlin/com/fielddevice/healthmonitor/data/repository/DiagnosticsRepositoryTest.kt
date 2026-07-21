package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.model.BatteryHealth
import com.fielddevice.healthmonitor.data.model.BatteryInfo
import com.fielddevice.healthmonitor.data.model.ChargingState
import com.fielddevice.healthmonitor.data.model.ConnectionType
import com.fielddevice.healthmonitor.data.model.DeploymentStatus
import com.fielddevice.healthmonitor.data.model.DeviceInfo
import com.fielddevice.healthmonitor.data.model.DiagnosticCheckId
import com.fielddevice.healthmonitor.data.model.NetworkInfo
import com.fielddevice.healthmonitor.data.model.PowerSource
import com.fielddevice.healthmonitor.data.model.StorageInfo
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class DiagnosticsRepositoryTest {

    private val deviceInfoRepository: DeviceInfoRepository = mockk()
    private val batteryRepository: BatteryRepository = mockk()
    private val storageRepository: StorageRepository = mockk()
    private val networkRepository: NetworkRepository = mockk()
    private val learningRecordRepository: LearningRecordRepository = mockk()

    private lateinit var repository: DiagnosticsRepositoryImpl

    private val healthyBattery = BatteryInfo(
        percentage = 80,
        chargingState = ChargingState.DISCHARGING,
        powerSource = PowerSource.BATTERY,
        health = BatteryHealth.GOOD,
        temperatureCelsius = 28f,
        voltageMillivolts = 4000
    )

    private val healthyStorage = StorageInfo(totalBytes = 100_000L, freeBytes = 60_000L)

    private val offlineNetwork = NetworkInfo(
        isConnected = false,
        connectionType = ConnectionType.NONE,
        isMetered = false,
        isValidated = false
    )

    private val sampleDevice = DeviceInfo(
        manufacturer = "Google",
        model = "Pixel Tablet",
        androidVersion = "14",
        sdkInt = 34,
        cpuAbi = "arm64-v8a",
        totalRamBytes = 8_000_000_000L,
        availableRamBytes = 3_000_000_000L,
        isLowRamDevice = false,
        totalStorageBytes = 64_000_000_000L,
        availableStorageBytes = 20_000_000_000L,
        screenWidthPx = 2560,
        screenHeightPx = 1600,
        densityDpi = 320
    )

    @Before
    fun setUp() {
        repository = DiagnosticsRepositoryImpl(
            deviceInfoRepository = deviceInfoRepository,
            batteryRepository = batteryRepository,
            storageRepository = storageRepository,
            networkRepository = networkRepository,
            learningRecordRepository = learningRecordRepository,
            clock = { 123L }
        )
    }

    @Test
    fun `all checks passing yields a READY report`() = runTest {
        every { batteryRepository.observeBatteryInfo() } returns flowOf(healthyBattery)
        coEvery { storageRepository.getStorageInfo() } returns healthyStorage
        every { networkRepository.observeNetworkInfo() } returns flowOf(offlineNetwork)
        coEvery { deviceInfoRepository.getDeviceInfo() } returns sampleDevice
        coEvery { learningRecordRepository.recordCount() } returns 3

        val report = repository.runDiagnostics()

        assertThat(report.passedCount).isEqualTo(5)
        assertThat(report.totalCount).isEqualTo(5)
        assertThat(report.deploymentStatus).isEqualTo(DeploymentStatus.READY)
        assertThat(report.generatedAtMillis).isEqualTo(123L)
    }

    @Test
    fun `offline network still counts as a passing check`() = runTest {
        every { batteryRepository.observeBatteryInfo() } returns flowOf(healthyBattery)
        coEvery { storageRepository.getStorageInfo() } returns healthyStorage
        every { networkRepository.observeNetworkInfo() } returns flowOf(offlineNetwork)
        coEvery { deviceInfoRepository.getDeviceInfo() } returns sampleDevice
        coEvery { learningRecordRepository.recordCount() } returns 0

        val report = repository.runDiagnostics()
        val networkCheck = report.checks.first { it.id == DiagnosticCheckId.NETWORK_CONNECTIVITY }

        assertThat(networkCheck.passed).isTrue()
        assertThat(networkCheck.detail).contains("Offline")
    }

    @Test
    fun `critical storage causes NEEDS_ATTENTION and a failed storage check`() = runTest {
        val criticalStorage = StorageInfo(totalBytes = 100_000L, freeBytes = 2_000L)

        every { batteryRepository.observeBatteryInfo() } returns flowOf(healthyBattery)
        coEvery { storageRepository.getStorageInfo() } returns criticalStorage
        every { networkRepository.observeNetworkInfo() } returns flowOf(offlineNetwork)
        coEvery { deviceInfoRepository.getDeviceInfo() } returns sampleDevice
        coEvery { learningRecordRepository.recordCount() } returns 0

        val report = repository.runDiagnostics()

        assertThat(report.deploymentStatus).isEqualTo(DeploymentStatus.NEEDS_ATTENTION)
        assertThat(report.passedCount).isEqualTo(4)
        val storageCheck = report.checks.first { it.id == DiagnosticCheckId.STORAGE_AVAILABILITY }
        assertThat(storageCheck.passed).isFalse()
    }

    @Test
    fun `a failing subsystem does not crash the whole report`() = runTest {
        every { batteryRepository.observeBatteryInfo() } throws RuntimeException("battery read failed")
        coEvery { storageRepository.getStorageInfo() } returns healthyStorage
        every { networkRepository.observeNetworkInfo() } returns flowOf(offlineNetwork)
        coEvery { deviceInfoRepository.getDeviceInfo() } returns sampleDevice
        coEvery { learningRecordRepository.recordCount() } returns 0

        val report = repository.runDiagnostics()
        val batteryCheck = report.checks.first { it.id == DiagnosticCheckId.BATTERY_STATUS }

        assertThat(batteryCheck.passed).isFalse()
        assertThat(report.deploymentStatus).isEqualTo(DeploymentStatus.NEEDS_ATTENTION)
    }
}
