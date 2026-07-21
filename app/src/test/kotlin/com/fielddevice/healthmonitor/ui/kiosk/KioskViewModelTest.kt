package com.fielddevice.healthmonitor.ui.kiosk

import com.fielddevice.healthmonitor.data.repository.KioskModeRepository
import com.fielddevice.healthmonitor.testutil.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class KioskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val backingState = MutableStateFlow(false)
    private val repository: KioskModeRepository = mockk {
        every { isDeploymentModeEnabled } returns backingState
    }

    @Test
    fun `exposes the repository's current deployment mode state`() {
        backingState.value = true
        val viewModel = KioskViewModel(repository)

        assertThat(viewModel.isDeploymentModeEnabled.value).isTrue()
    }

    @Test
    fun `setDeploymentModeEnabled delegates to the repository`() = runTest {
        coEvery { repository.setDeploymentModeEnabled(true) } returns Unit
        val viewModel = KioskViewModel(repository)

        viewModel.setDeploymentModeEnabled(true)

        coVerify { repository.setDeploymentModeEnabled(true) }
    }
}
