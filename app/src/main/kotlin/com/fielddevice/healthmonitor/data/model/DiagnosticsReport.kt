package com.fielddevice.healthmonitor.data.model

/**
 * One check performed by the "Run Device Check" test suite.
 */
enum class DiagnosticCheckId(val label: String) {
    BATTERY_STATUS("Battery status"),
    STORAGE_AVAILABILITY("Storage availability"),
    NETWORK_CONNECTIVITY("Network connectivity"),
    DEVICE_INFO_AVAILABLE("Device information availability"),
    DATABASE_AVAILABLE("Database availability")
}

data class DiagnosticCheckResult(
    val id: DiagnosticCheckId,
    val passed: Boolean,
    val detail: String
)

/**
 * Full result of a diagnostics run, i.e. the "DEVICE READINESS REPORT" the field team sees
 * after tapping "Run Device Check". A device is only [DeploymentStatus.READY] when every
 * single check passes — a partial pass means something needs attention before the tablet
 * ships to a classroom or clinic.
 */
data class DiagnosticsReport(
    val checks: List<DiagnosticCheckResult>,
    val generatedAtMillis: Long
) {
    val passedCount: Int get() = checks.count { it.passed }
    val totalCount: Int get() = checks.size

    val deploymentStatus: DeploymentStatus
        get() = if (passedCount == totalCount) DeploymentStatus.READY else DeploymentStatus.NEEDS_ATTENTION
}

enum class DeploymentStatus {
    READY,
    NEEDS_ATTENTION
}
