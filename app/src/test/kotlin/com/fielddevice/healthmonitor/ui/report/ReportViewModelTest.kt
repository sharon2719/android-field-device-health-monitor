package com.fielddevice.healthmonitor.ui.report

import com.fielddevice.healthmonitor.data.model.DeviceReport
import com.fielddevice.healthmonitor.data.repository.ReportRepository
import com.fielddevice.healthmonitor.testutil.MainDispatcherRule
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: ReportRepository = mockk()

    @Test
    fun `generateReport populates the report on success`() = runTest {
        val report = DeviceReport(reportText = "ANDROID DEVICE REPORT", generatedAtMillis = 42L)
        coEvery { repository.generateReport() } returns report

        val viewModel = ReportViewModel(repository)
        viewModel.generateReport()

        val state = viewModel.uiState.value
        assertThat(state.isGenerating).isFalse()
        assertThat(state.report).isEqualTo(report)
        assertThat(state.error).isNull()
    }

    @Test
    fun `generateReport surfaces an error on failure`() = runTest {
        coEvery { repository.generateReport() } throws RuntimeException("io error")

        val viewModel = ReportViewModel(repository)
        viewModel.generateReport()

        val state = viewModel.uiState.value
        assertThat(state.report).isNull()
        assertThat(state.error).isEqualTo("io error")
    }
}
