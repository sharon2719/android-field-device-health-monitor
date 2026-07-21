package com.fielddevice.healthmonitor.data.model

/**
 * Simulated over-the-air update package for a fleet of learning tablets. This models the
 * *concepts* a real MDM/OTA pipeline needs — semantic versioning, package validation before
 * install, and having a rollback plan — without touching real firmware or system partitions
 * (that requires privileged system/OEM APIs far outside what a normal app can or should do).
 */
data class OtaUpdateInfo(
    val currentVersion: String,
    val availableVersion: String,
    val updatePackageName: String,
    val packageSizeBytes: Long,
    val checksumSha256: String,
    val status: OtaUpdateStatus,
    val rollbackVersion: String?
) {
    /** Simple semantic-version string comparison, e.g. "1.0.0" -> "1.0.1". */
    val isUpdateAvailable: Boolean
        get() = compareVersions(availableVersion, currentVersion) > 0

    private fun compareVersions(a: String, b: String): Int {
        val partsA = a.split(".").map { it.toIntOrNull() ?: 0 }
        val partsB = b.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(partsA.size, partsB.size)) {
            val diff = (partsA.getOrElse(i) { 0 }) - (partsB.getOrElse(i) { 0 })
            if (diff != 0) return diff
        }
        return 0
    }
}

/**
 * Lifecycle of a simulated update, from discovery through validation to a state where a
 * rollback point has been captured in case the install needs to be reversed.
 */
enum class OtaUpdateStatus(val displayName: String) {
    UP_TO_DATE("Up to date"),
    READY("Ready"),
    VALIDATING("Validating package"),
    VALIDATION_FAILED("Validation failed"),
    ROLLBACK_PREPARED("Rollback prepared"),
    INSTALLING("Installing"),
    INSTALLED("Installed")
}
