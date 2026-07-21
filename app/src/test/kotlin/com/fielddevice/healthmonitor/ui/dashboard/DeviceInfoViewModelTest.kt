package com.fielddevice.healthmonitor.ui.dashboard

import com.fielddevice.healthmonitor.data.model.DeviceInfo
import com.fielddevice.healthmonitor.data.repository.DeviceInfoRepository
import com.fielddevice.healthmonitor.testutil.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeviceInfoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DeviceInfoRepository = mockk()

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

    @Test
    fun `loads device info on init and exposes it as not loading`() = runTest {
        coEvery { repository.getDeviceInfo() } returns sampleDevice

        val viewModel = DeviceInfoViewModel(repository)
        val state = viewModel.uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.deviceInfo).isEqualTo(sampleDevice)
        assertThat(state.error).isNull()
    }

    @Test
    fun `surfaces an error message when the repository throws`() = runTest {
        coEvery { repository.getDeviceInfo() } throws RuntimeException("StatFs failed")

        val viewModel = DeviceInfoViewModel(repository)
        val state = viewModel.uiState.value

        assertThat(state.isLoading).isFalse()
        assertThat(state.deviceInfo).isNull()
        assertThat(state.error).isEqualTo("StatFs failed")
    }
}
