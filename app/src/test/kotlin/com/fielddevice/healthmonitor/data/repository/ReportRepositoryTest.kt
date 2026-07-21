package com.fielddevice.healthmonitor.data.repository

import com.fielddevice.healthmonitor.data.model.BatteryHealth
import com.fielddevice.healthmonitor.data.model.BatteryInfo
import com.fielddevice.healthmonitor.data.model.ChargingState
import com.fielddevice.healthmonitor.data.model.ConnectionType
import com.fielddevice.healthmonitor.data.model.DeviceInfo
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

class ReportRepositoryTest {

    private val deviceInfoRepository: DeviceInfoRepository = mockk()
    private val batteryRepository: BatteryRepository = mockk()
    private val storageRepository: StorageRepository = mockk()
    private val networkRepository: NetworkRepository = mockk()
    private val learningRecordRepository: LearningRecordRepository = mockk()

    private lateinit var repository: ReportRepositoryImpl

    @Before
    fun setUp() {
        repository = ReportRepositoryImpl(
            deviceInfoRepository = deviceInfoRepository,
            batteryRepository = batteryRepository,
            storageRepository = storageRepository,
            networkRepository = networkRepository,
            learningRecordRepository = learningRecordRepository,
            clock = { FIXED_TIME }
        )

        coEvery { deviceInfoRepository.getDeviceInfo() } returns DeviceInfo(
            manufacturer = "Samsung",
            model = "Galaxy Tab",
            androidVersion = "14",
            sdkInt = 34,
            cpuAbi = "arm64-v8a",
            totalRamBytes = 4_000_000_000L,
            availableRamBytes = 1_000_000_000L,
            isLowRamDevice = false,
            totalStorageBytes = 32_000_000_000L,
            availableStorageBytes = 8_000_000_000L,
            screenWidthPx = 1920,
            screenHeightPx = 1200,
            densityDpi = 240
        )
        every { batteryRepository.observeBatteryInfo() } returns flowOf(
            BatteryInfo(
                percentage = 82,
                chargingState = ChargingState.DISCHARGING,
                powerSource = PowerSource.BATTERY,
                health = BatteryHealth.GOOD,
                temperatureCelsius = 27f,
                voltageMillivolts = 3900
            )
        )
        coEvery { storageRepository.getStorageInfo() } returns StorageInfo(
            totalBytes = 32_000_000_000L,
            freeBytes = 8_000_000_000L
        )
        every { networkRepository.observeNetworkInfo() } returns flowOf(
            NetworkInfo(isConnected = false, connectionType = ConnectionType.NONE, isMetered = false, isValidated = false)
        )
        every { learningRecordRepository.observePendingCount() } returns flowOf(2)
        coEvery { learningRecordRepository.recordCount() } returns 5
    }

    @Test
    fun `generated report includes every required section`() = runTest {
        val report = repository.generateReport()

        assertThat(report.reportText).contains("ANDROID DEVICE REPORT")
        assertThat(report.reportText).contains("Model:")
        assertThat(report.reportText).contains("Samsung Galaxy Tab")
        assertThat(report.reportText).contains("Android:")
        assertThat(report.reportText).contains("Battery:")
        assertThat(report.reportText).contains("82%")
        assertThat(report.reportText).contains("Storage:")
        assertThat(report.reportText).contains("Network:")
        assertThat(report.reportText).contains("Offline")
        assertThat(report.reportText).contains("Database:")
        assertThat(report.reportText).contains("Generated:")
        assertThat(report.generatedAtMillis).isEqualTo(FIXED_TIME)
    }

    companion object {
        private const val FIXED_TIME = 1_700_000_000_000L
    }
}
