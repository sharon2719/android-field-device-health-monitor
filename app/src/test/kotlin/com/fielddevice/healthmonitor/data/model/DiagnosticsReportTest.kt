package com.fielddevice.healthmonitor.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DiagnosticsReportTest {

    private fun result(id: DiagnosticCheckId, passed: Boolean) =
        DiagnosticCheckResult(id = id, passed = passed, detail = "detail")

    @Test
    fun `all checks passing yields READY`() {
        val report = DiagnosticsReport(
            checks = DiagnosticCheckId.entries.map { result(it, passed = true) },
            generatedAtMillis = 0L
        )

        assertThat(report.passedCount).isEqualTo(report.totalCount)
        assertThat(report.deploymentStatus).isEqualTo(DeploymentStatus.READY)
    }

    @Test
    fun `a single failing check yields NEEDS_ATTENTION`() {
        val checks = DiagnosticCheckId.entries.mapIndexed { index, id ->
            result(id, passed = index != 0)
        }
        val report = DiagnosticsReport(checks = checks, generatedAtMillis = 0L)

        assertThat(report.passedCount).isEqualTo(report.totalCount - 1)
        assertThat(report.deploymentStatus).isEqualTo(DeploymentStatus.NEEDS_ATTENTION)
    }
}
