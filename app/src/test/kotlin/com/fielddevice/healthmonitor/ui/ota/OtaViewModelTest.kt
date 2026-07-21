package com.fielddevice.healthmonitor.ui.ota

import com.fielddevice.healthmonitor.data.model.OtaUpdateInfo
import com.fielddevice.healthmonitor.data.model.OtaUpdateStatus
import com.fielddevice.healthmonitor.data.repository.OtaUpdateRepository
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

class OtaViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val backingState = MutableStateFlow(
        OtaUpdateInfo(
            currentVersion = "1.0.0",
            availableVersion = "1.0.1",
            updatePackageName = "learning-platform-update.zip",
            packageSizeBytes = 1_000_000L,
            checksumSha256 = "sha256:abc",
            status = OtaUpdateStatus.READY,
            rollbackVersion = null
        )
    )
    private val repository: OtaUpdateRepository = mockk {
        every { updateState } returns backingState
    }

    @Test
    fun `exposes the repository's current update state`() {
        val viewModel = OtaViewModel(repository)

        assertThat(viewModel.updateState.value.status).isEqualTo(OtaUpdateStatus.READY)
        assertThat(viewModel.updateState.value.availableVersion).isEqualTo("1.0.1")
    }

    @Test
    fun `validateAndPrepareRollback delegates to the repository`() = runTest {
        coEvery { repository.validateAndPrepareRollback() } returns Unit
        val viewModel = OtaViewModel(repository)

        viewModel.validateAndPrepareRollback()

        coVerify { repository.validateAndPrepareRollback() }
    }

    @Test
    fun `installUpdate delegates to the repository`() = runTest {
        coEvery { repository.installUpdate() } returns Unit
        val viewModel = OtaViewModel(repository)

        viewModel.installUpdate()

        coVerify { repository.installUpdate() }
    }
}
