package com.fielddevice.healthmonitor.ui.diagnostics

import com.fielddevice.healthmonitor.data.model.DeploymentStatus
import com.fielddevice.healthmonitor.data.model.DiagnosticCheckId
import com.fielddevice.healthmonitor.data.model.DiagnosticCheckResult
import com.fielddevice.healthmonitor.data.model.DiagnosticsReport
import com.fielddevice.healthmonitor.data.repository.DiagnosticsRepository
import com.fielddevice.healthmonitor.testutil.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DiagnosticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: DiagnosticsRepository = mockk()

    private fun readyReport() = DiagnosticsReport(
        checks = DiagnosticCheckId.entries.map { DiagnosticCheckResult(it, passed = true, detail = "ok") },
        generatedAtMillis = 1L
    )

    @Test
    fun `initial state is idle with no report`() {
        val viewModel = DiagnosticsViewModel(repository)

        assertThat(viewModel.uiState.value.isRunning).isFalse()
        assertThat(viewModel.uiState.value.report).isNull()
    }

    @Test
    fun `runDeviceCheck populates the report on success`() = runTest {
        coEvery { repository.runDiagnostics() } returns readyReport()
        val viewModel = DiagnosticsViewModel(repository)

        viewModel.runDeviceCheck()

        val state = viewModel.uiState.value
        assertThat(state.isRunning).isFalse()
        assertThat(state.report?.deploymentStatus).isEqualTo(DeploymentStatus.READY)
    }

    @Test
    fun `runDeviceCheck surfaces an error message on failure`() = runTest {
        coEvery { repository.runDiagnostics() } throws RuntimeException("boom")
        val viewModel = DiagnosticsViewModel(repository)

        viewModel.runDeviceCheck()

        val state = viewModel.uiState.value
        assertThat(state.isRunning).isFalse()
        assertThat(state.report).isNull()
        assertThat(state.error).isEqualTo("boom")
    }
}
