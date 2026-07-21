package com.fielddevice.healthmonitor.data.model

/**
 * A generated, human-readable diagnostics report — the artifact a field technician can
 * copy/share as proof a tablet was checked before being handed to a classroom or clinic.
 */
data class DeviceReport(
    val reportText: String,
    val generatedAtMillis: Long
)
